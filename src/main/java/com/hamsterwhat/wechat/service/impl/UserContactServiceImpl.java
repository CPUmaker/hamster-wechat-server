package com.hamsterwhat.wechat.service.impl;

import com.hamsterwhat.wechat.entity.constants.RedisConstants;
import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.dto.SystemSettingsDTO;
import com.hamsterwhat.wechat.entity.dto.UserContactSearchResultDTO;
import com.hamsterwhat.wechat.entity.enums.*;
import com.hamsterwhat.wechat.entity.po.*;
import com.hamsterwhat.wechat.entity.query.BaseParam;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.mapper.*;
import com.hamsterwhat.wechat.service.UserContactService;
import com.hamsterwhat.wechat.utils.RedisUtils;
import com.hamsterwhat.wechat.utils.StringUtils;
import com.hamsterwhat.wechat.utils.SystemSettings;
import com.hamsterwhat.wechat.websocket.netty.handler.MessageHandler;
import com.hamsterwhat.wechat.websocket.netty.utils.SessionManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class UserContactServiceImpl implements UserContactService {

    private final UserContactMapper userContactMapper;

    private final UserInfoMapper userInfoMapper;

    private final GroupInfoMapper groupInfoMapper;

    private final ChatSessionMapper chatSessionMapper;

    private final ChatMessageMapper chatMessageMapper;

    private final ChatSessionUserMapper chatSessionUserMapper;

    private final SystemSettings systemSettings;

    private final RedisUtils redisUtils;

    private final MessageHandler messageHandler;

    @Autowired
    public UserContactServiceImpl(
            UserContactMapper userContactMapper,
            UserInfoMapper userInfoMapper,
            GroupInfoMapper groupInfoMapper,
            ChatSessionMapper chatSessionMapper,
            ChatMessageMapper chatMessageMapper,
            ChatSessionUserMapper chatSessionUserMapper,
            SystemSettings systemSettings,
            RedisUtils redisUtils,
            MessageHandler messageHandler
    ) {
        this.userContactMapper = userContactMapper;
        this.userInfoMapper = userInfoMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.chatSessionUserMapper = chatSessionUserMapper;
        this.systemSettings = systemSettings;
        this.redisUtils = redisUtils;
        this.messageHandler = messageHandler;
    }

    @Override
    public UserContact getUserContactByPrimaryKeys(String userId, String contactId) {
        return this.userContactMapper.selectUserContactByPrimaryKeys(
                userId, contactId
        );
    }

    @Override
    public List<UserContact> findListByParam(UserContactQuery param) {
        return this.userContactMapper.selectUserContactListByQuery(param);
    }

    @Override
    public Integer findCountByParam(UserContactQuery param) {
        return this.userContactMapper.selectUserContactCountByQuery(param);
    }

    @Override
    public UserContactSearchResultDTO searchContact(String userId, String contactorId) {
        UserContactTypeEnum contactType = UserContactTypeEnum.getByPrefix(contactorId.substring(0, 1));
        UserContactSearchResultDTO resultDTO = new UserContactSearchResultDTO();
        switch (contactType) {
            case USER:
                UserInfo userInfo = this.userInfoMapper.selectUserInfoByUserId(contactorId);
                if (userInfo == null) {
                    return null;
                }
                resultDTO.setContactorName(userInfo.getUsername());
                resultDTO.setGender(userInfo.getGender());
                resultDTO.setGeo(userInfo.getGeo());
                break;
            case GROUP:
                GroupInfo groupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(contactorId);
                if (groupInfo == null) {
                    return null;
                }
                resultDTO.setContactorName(groupInfo.getGroupName());
                break;
            default:
                throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        resultDTO.setContactorId(contactorId);
        resultDTO.setContactType(contactType.getDescription());

        if (userId.equals(contactorId)) {
            resultDTO.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return resultDTO;
        }

        UserContact userContact = this.userContactMapper.selectUserContactByPrimaryKeys(
                userId, contactorId
        );
        resultDTO.setStatus(userContact == null ?
                UserContactStatusEnum.NOT_CONNECTED.getStatus() : userContact.getStatus());
        return resultDTO;
    }

    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Short contactType, String applyMsg) {
        // Check group member count
        if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            UserContactQuery query = new UserContactQuery();
            query.setContactorId(contactId);
            query.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            Integer count = this.userContactMapper.selectUserContactCountByQuery(query);
            if (this.systemSettings.isExceedGroupCountLimit(count)) {
                throw new BusinessException("This group is exceed group member count limit!");
            }
        }

        // The applicant adds new contact for applicant and
        insertOrUpdateUserContact(
                applyUserId, contactId, contactType, UserContactStatusEnum.FRIEND.getStatus());

        // The contactor adds new contact for contactor (group don't need to set the relation)
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            insertOrUpdateUserContact(
                    contactId, applyUserId, contactType, UserContactStatusEnum.FRIEND.getStatus());
        }

        // create session
        Date currentTime = new Date();
        String sessionId;
        MessageDTO<Object> messageDTO = new MessageDTO<>();
        if (UserContactTypeEnum.USER.getType().equals(contactType)) {
            UserInfo applyUserInfo = this.userInfoMapper.selectUserInfoByUserId(applyUserId);
            UserInfo contactUserInfo = this.userInfoMapper.selectUserInfoByUserId(contactId);
            if (applyUserInfo == null || contactUserInfo == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }

            sessionId = StringUtils.getSessionId(applyUserId, contactId);
            ChatSession session = new ChatSession();
            session.setId(sessionId);
            session.setLastMessage(applyMsg);
            session.setLastReceiveTime(currentTime);
            this.chatSessionMapper.insertOrUpdateChatSession(session);

            ChatSessionUser sessionUser = new ChatSessionUser();
            sessionUser.setUserId(applyUserId);
            sessionUser.setContactorId(contactId);
            sessionUser.setSessionId(sessionId);
            sessionUser.setContactorName(contactUserInfo.getUsername());
            this.chatSessionUserMapper.insertOrUpdateChatSessionUser(sessionUser);

            ChatSessionUser sessionContact = new ChatSessionUser();
            sessionContact.setUserId(contactId);
            sessionContact.setContactorId(applyUserId);
            sessionContact.setSessionId(sessionId);
            sessionContact.setContactorName(applyUserInfo.getUsername());
            this.chatSessionUserMapper.insertOrUpdateChatSessionUser(sessionContact);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(CommandTypeEnum.ADD_FRIEND.getType());
            chatMessage.setContent(applyMsg);
            chatMessage.setSendUserId(applyUserId);
            chatMessage.setSendUserUsername(applyUserInfo.getUsername());
            chatMessage.setSendTime(currentTime);
            chatMessage.setContactorId(contactId);
            chatMessage.setContactType(UserContactTypeEnum.USER.getType());
            chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
            this.chatMessageMapper.insertChatMessage(chatMessage);

            BeanUtils.copyProperties(chatMessage, messageDTO);
            messageDTO.setContactorName(contactUserInfo.getUsername());
        } else if (UserContactTypeEnum.GROUP.getType().equals(contactType)) {
            UserInfo applyUserInfo = this.userInfoMapper.selectUserInfoByUserId(applyUserId);
            GroupInfo contactGroupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(contactId);
            if (applyUserInfo == null || contactGroupInfo == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }

            // Prepare data
            sessionId = StringUtils.getSessionId(contactId);
            String messageContent = String.format(
                    SystemConstants.DEFAULT_JOIN_GROUP_MSG, applyUserInfo.getUsername());

            ChatSession session = new ChatSession();
            session.setId(sessionId);
            session.setLastMessage(messageContent);
            session.setLastReceiveTime(currentTime);
            this.chatSessionMapper.insertOrUpdateChatSession(session);

            ChatSessionUser sessionUser = new ChatSessionUser();
            sessionUser.setUserId(applyUserId);
            sessionUser.setContactorId(contactId);
            sessionUser.setSessionId(sessionId);
            sessionUser.setContactorName(contactGroupInfo.getGroupName());
            this.chatSessionUserMapper.insertOrUpdateChatSessionUser(sessionUser);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(CommandTypeEnum.JOIN_GROUP.getType());
            chatMessage.setContent(messageContent);
            chatMessage.setSendUserId(contactId);
            chatMessage.setSendUserUsername(contactGroupInfo.getGroupName());
            chatMessage.setSendTime(currentTime);
            chatMessage.setContactorId(contactId);
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
            this.chatMessageMapper.insertChatMessage(chatMessage);

            // Add user to group channel
            SessionManager.bindChannelGroup(contactGroupInfo.getGroupId(), applyUserId);

            BeanUtils.copyProperties(chatMessage, messageDTO);
            messageDTO.setContactorName(contactGroupInfo.getGroupName());
        } else {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        // Send message
        this.messageHandler.sendMessage(messageDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRobotContact(String userId) {
        Date currTime = new Date();
        SystemSettingsDTO settings = this.systemSettings.getSystemSettings();
        String robotId = settings.getRobotId();
        String robotName = settings.getRobotNickName();
        String initMessage = StringUtils.cleanHtmlTag(settings.getRobotWelcome());

        // Add contact record
        UserContact userContact = new UserContact();
        userContact.setUserId(userId);
        userContact.setContactorId(robotId);
        userContact.setContactType(UserContactTypeEnum.USER.getType());
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        this.userContactMapper.insertUserContact(userContact);

        // create session info
        String sessionId = StringUtils.getSessionId(userId, robotId);
        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);
        chatSession.setLastMessage(initMessage);
        chatSession.setLastReceiveTime(currTime);
        this.chatSessionMapper.insertChatSession(chatSession);

        // create session user
        ChatSessionUser sessionUser = new ChatSessionUser();
        sessionUser.setUserId(userId);
        sessionUser.setContactorId(robotId);
        sessionUser.setSessionId(sessionId);
        sessionUser.setContactorName(robotName);
        this.chatSessionUserMapper.insertChatSessionUser(sessionUser);

        // create chat message
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(CommandTypeEnum.CHAT_MESSAGE.getType());
        chatMessage.setContent(initMessage);
        chatMessage.setSendUserId(robotId);
        chatMessage.setSendUserUsername(robotName);
        chatMessage.setContactorId(userId);
        chatMessage.setContactType(UserContactTypeEnum.USER.getType());
        chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        chatMessage.setSendTime(currTime);
        this.chatMessageMapper.insertChatMessage(chatMessage);
    }

    @Override
    public List<UserContact> getAllContacts(String userId, Short contactType) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByType(contactType);

        UserContactQuery query = new UserContactQuery();
        query.setUserId(userId);
        query.setContactType(contactType);
        query.setStatusIn(new Short[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DELETED_BY_OTHER.getStatus(),
                UserContactStatusEnum.BLOCKED_BY_OTHER.getStatus()
        });

        if (contactTypeEnum == UserContactTypeEnum.USER) {
            query.setIncludesUserContactor(BaseParam.TableJoinType.INNER);
        } else if (contactTypeEnum == UserContactTypeEnum.GROUP) {
            query.setIncludesGroupContactor(BaseParam.TableJoinType.INNER);
        }
        query.setOrderBy("uc.created_at");
        query.setOrderDirection("DESC");

        return this.userContactMapper.selectUserContactListByQuery(query);
    }

    @Override
    public Integer deleteContact(String userId, String contactId, boolean isToBlock) {
        // delete cache
        this.redisUtils.removeFromSet(RedisConstants.USER_CONTACT_KEY + userId, contactId);
        this.redisUtils.removeFromSet(RedisConstants.USER_CONTACT_KEY + contactId, userId);

        // modify relation "user -> contact" with flag DELETED
        UserContact userContact = new UserContact();
        userContact.setStatus(UserContactStatusEnum.DELETED.getStatus());
        UserContactQuery query = new UserContactQuery();
        query.setUserId(userId);
        query.setContactorId(contactId);
        query.setStatus(UserContactStatusEnum.FRIEND.getStatus()); // If DELETED or BLOCKED, no update
        return this.userContactMapper.updateUserContactByQuery(userContact, query);
    }

    private void insertOrUpdateUserContact(String userId, String contactorId, Short contactType, Short status) {
        UserContact newContact = new UserContact();
        newContact.setUserId(userId);
        newContact.setContactorId(contactorId);
        newContact.setContactType(contactType);
        newContact.setStatus(status);
        this.userContactMapper.insertOrUpdateUserContact(newContact);
        this.redisUtils.addToSet(
                RedisConstants.USER_CONTACT_KEY + userId,
                RedisConstants.USER_CONTACT_EXPIRE,
                contactorId
        );
    }
}
