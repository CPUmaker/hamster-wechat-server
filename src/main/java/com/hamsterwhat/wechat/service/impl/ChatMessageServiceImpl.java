package com.hamsterwhat.wechat.service.impl;

import com.hamsterwhat.wechat.entity.constants.AppProperties;
import com.hamsterwhat.wechat.entity.constants.RedisConstants;
import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.dto.SystemSettingsDTO;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.enums.*;
import com.hamsterwhat.wechat.entity.po.ChatMessage;
import com.hamsterwhat.wechat.entity.po.ChatSession;
import com.hamsterwhat.wechat.entity.po.GroupInfo;
import com.hamsterwhat.wechat.entity.po.UserInfo;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.mapper.*;
import com.hamsterwhat.wechat.service.ChatMessageService;
import com.hamsterwhat.wechat.utils.RedisUtils;
import com.hamsterwhat.wechat.utils.StringUtils;
import com.hamsterwhat.wechat.utils.SystemSettings;
import com.hamsterwhat.wechat.utils.UploadUtils;
import com.hamsterwhat.wechat.websocket.netty.handler.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final UserInfoMapper userInfoMapper;

    private final GroupInfoMapper groupInfoMapper;

    private final UserContactMapper userContactMapper;

    private final ChatMessageMapper chatMessageMapper;

    private final ChatSessionMapper chatSessionMapper;

    private final RedisUtils redisUtils;

    private final MessageHandler messageHandler;

    private final SystemSettings systemSettings;

    private final AppProperties appProperties;

    @Autowired
    public ChatMessageServiceImpl(
            UserInfoMapper userInfoMapper,
            GroupInfoMapper groupInfoMapper,
            UserContactMapper userContactMapper,
            ChatMessageMapper chatMessageMapper,
            ChatSessionMapper chatSessionMapper,
            RedisUtils redisUtils,
            MessageHandler messageHandler,
            SystemSettings systemSettings,
            AppProperties appProperties
    ) {
        this.userInfoMapper = userInfoMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.userContactMapper = userContactMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.redisUtils = redisUtils;
        this.messageHandler = messageHandler;
        this.systemSettings = systemSettings;
        this.appProperties = appProperties;
    }

    @Override
    public ChatMessage getChatMessageById(Long messageId) {
        return this.chatMessageMapper.selectChatMessageById(messageId);
    }

    @Override
    public MessageDTO<Serializable> saveMessage(ChatMessage chatMessage, TokenUserInfoDTO token) {
        String sendUserId = token.getUserId();
        String contactorId = chatMessage.getContactorId();
        String contactKey = RedisConstants.USER_CONTACT_KEY + sendUserId;

        boolean isRobot = contactorId.equals(SystemConstants.ROBOT_ID);
        boolean isRobotSelf = sendUserId.equals(SystemConstants.ROBOT_ID);
        boolean isContact = this.redisUtils.isMemberOfSet(contactKey, chatMessage.getContactorId());
        UserContactTypeEnum contactType = UserContactTypeEnum.getByPrefix(contactorId.substring(0, 1));
        if (!isRobot && !isRobotSelf && !isContact) {
            if (contactType == UserContactTypeEnum.USER) {
                throw new BusinessException(ResponseCodeEnum.CODE_902);
            }
            if (contactType == UserContactTypeEnum.GROUP) {
                throw new BusinessException(ResponseCodeEnum.CODE_903);
            }
        }

        Date currentTime = new Date();
        String sessionId = null;
        String contactorName = null;
        if (contactType == UserContactTypeEnum.USER) {
            sessionId = StringUtils.getSessionId(sendUserId, contactorId);
            UserInfo contactorUserInfo = this.userInfoMapper.selectUserInfoByUserId(contactorId);
            if (contactorUserInfo != null) {
                contactorName = contactorUserInfo.getUsername();
            }
        } else if (contactType == UserContactTypeEnum.GROUP) {
            sessionId = StringUtils.getSessionId(contactorId);
            GroupInfo contactorGroupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(contactorId);
            if (contactorGroupInfo != null) {
                contactorName = contactorGroupInfo.getGroupName();
            }
        }

        chatMessage.setSessionId(sessionId);
        chatMessage.setContent(
                StringUtils.cleanHtmlTag(chatMessage.getContent())
        );
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserUsername(token.getUsername());
        chatMessage.setSendTime(currentTime);
        chatMessage.setContactType(contactType.getType());
        this.chatMessageMapper.insertChatMessage(chatMessage);

        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);
        chatSession.setLastMessage(chatMessage.getContent());
        chatSession.setLastReceiveTime(currentTime);
        if (contactType == UserContactTypeEnum.GROUP) {
            chatSession.setLastMessage(token.getUsername() + chatSession.getLastMessage());
        }
        this.chatSessionMapper.updateChatSession(chatSession);

        MessageDTO<Serializable> messageDTO = null;
        if (isRobot) {
            // Prepare robot response
            SystemSettingsDTO settings = this.systemSettings.getSystemSettings();
            TokenUserInfoDTO tokenUserInfoDTO = new TokenUserInfoDTO();
            tokenUserInfoDTO.setUserId(settings.getRobotId());
            tokenUserInfoDTO.setUsername(settings.getRobotNickName());
            ChatMessage robotChatMessage = new ChatMessage();
            robotChatMessage.setMessageType(CommandTypeEnum.CHAT_MESSAGE.getType());
            robotChatMessage.setContent("New function is implementing!");
            robotChatMessage.setContactorId(sendUserId);
            robotChatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
            saveMessage(robotChatMessage, token);
        } else {
            // Send message
            messageDTO = new MessageDTO<>();
            BeanUtils.copyProperties(chatMessage, messageDTO);
            messageDTO.setContactorName(contactorName);
            this.messageHandler.sendMessage(messageDTO);
        }

        return messageDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <T extends Serializable> void sendGroupNotificationMessage(
            String groupId, Short messageType, String content, T extendData) {
        Date currentTime = new Date();
        String sessionId = StringUtils.getSessionId(groupId);
        GroupInfo groupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(groupId);
        if (groupInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);
        chatSession.setLastMessage(content);
        chatSession.setLastReceiveTime(currentTime);
        this.chatSessionMapper.updateChatSession(chatSession);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(messageType);
        chatMessage.setContent(content);
        chatMessage.setSendUserId(groupId);
        chatMessage.setSendUserUsername(groupInfo.getGroupName());
        chatMessage.setSendTime(currentTime);
        chatMessage.setContactorId(groupId);
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        this.chatMessageMapper.insertChatMessage(chatMessage);

        // Create and send message
        MessageDTO<T> messageDTO = new MessageDTO<>();
        BeanUtils.copyProperties(chatMessage, messageDTO);
        messageDTO.setContactorName(groupInfo.getGroupName());
        messageDTO.setExtendData(extendData);
        this.messageHandler.sendMessage(messageDTO);
    }

    @Override
    public void uploadFile(String userId, Long messageId, MultipartFile file) {
        ChatMessage chatMessage = this.chatMessageMapper.selectChatMessageById(messageId);
        if (chatMessage == null || chatMessage.getSendUserId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String suffix = StringUtils.getFileSuffix(file.getOriginalFilename());
        if (suffix == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        boolean isImage = ArrayUtils.contains(SystemConstants.ALLOWED_IMAGE_EXTENSIONS, suffix.toLowerCase());
        boolean isVideo = ArrayUtils.contains(SystemConstants.ALLOWED_VIDEO_EXTENSIONS, suffix.toLowerCase());
        LocalDate localDate = LocalDate.from(chatMessage.getSendTime().toInstant().atZone(ZoneId.systemDefault()));
        String yearAndMonth = localDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String fileName = messageId + suffix;
        String path;

        if (isImage && !this.systemSettings.isExceedImageSizeLimit(file.getSize())) {
            path = this.appProperties.getProject().getFolder() +
                    SystemConstants.UPLOAD_IMAGE_FOLDER + File.separator + yearAndMonth;
        } else if (isVideo && !this.systemSettings.isExceedVideoSizeLimit(file.getSize())) {
            path = this.appProperties.getProject().getFolder() +
                    SystemConstants.UPLOAD_VIDEO_FOLDER + File.separator + yearAndMonth;
        } else if (!this.systemSettings.isExceedFileSizeLimit(file.getSize())) {
            path = this.appProperties.getProject().getFolder() +
                    SystemConstants.UPLOAD_FILE_FOLDER + File.separator + yearAndMonth;
        } else {
            throw new BusinessException("The file size limit exceeded!");
        }
        UploadUtils.uploadFile(path, fileName, file);

        ChatMessage uploadMessage = new ChatMessage();
        uploadMessage.setId(messageId);
        uploadMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        this.chatMessageMapper.updateChatMessage(uploadMessage);

        MessageDTO<Long> messageDTO = new MessageDTO<>();
        messageDTO.setMessageType(CommandTypeEnum.FILE_UPLOAD.getType());
        messageDTO.setSendUserId(chatMessage.getSendUserId());
        messageDTO.setContactorId(chatMessage.getContactorId());
        messageDTO.setExtendData(messageId);
        this.messageHandler.sendMessage(messageDTO);
    }

    @Override
    public Path getMessageFilePath(String userId, Long messageId, Path filePath, boolean showCover) {
        // Validate permission
        ChatMessage chatMessage = this.chatMessageMapper.selectChatMessageById(messageId);
        if (chatMessage == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserContactTypeEnum contactType = UserContactTypeEnum.getByType(chatMessage.getContactType());
        if (contactType == UserContactTypeEnum.USER && chatMessage.getContactorId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (contactType == UserContactTypeEnum.GROUP) {
            UserContactQuery query = new UserContactQuery();
            query.setUserId(userId);
            query.setContactorId(chatMessage.getContactorId());
            query.setContactType(UserContactTypeEnum.GROUP.getType());
            query.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = this.userContactMapper.selectUserContactCountByQuery(query);
            if (count == 0) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        // process file path
        LocalDate localDate = LocalDate.from(chatMessage.getSendTime().toInstant().atZone(ZoneId.systemDefault()));
        String yearAndMonth = localDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String fileName = messageId
                + (showCover ? SystemConstants.COVER_SUFFIX : "")
                + StringUtils.getFileSuffix(chatMessage.getFileName());
        return filePath.resolve(yearAndMonth).resolve(fileName);
    }
}
