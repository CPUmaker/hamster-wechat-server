package com.hamsterwhat.wechat.service.impl;

import com.hamsterwhat.wechat.entity.constants.AppProperties;
import com.hamsterwhat.wechat.entity.constants.RedisConstants;
import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.enums.*;
import com.hamsterwhat.wechat.entity.po.*;
import com.hamsterwhat.wechat.entity.query.GroupInfoQuery;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.mapper.*;
import com.hamsterwhat.wechat.service.ChatMessageService;
import com.hamsterwhat.wechat.service.ChatSessionUserService;
import com.hamsterwhat.wechat.service.GroupInfoService;
import com.hamsterwhat.wechat.service.UserContactService;
import com.hamsterwhat.wechat.utils.RedisUtils;
import com.hamsterwhat.wechat.utils.StringUtils;
import com.hamsterwhat.wechat.utils.SystemSettings;
import com.hamsterwhat.wechat.utils.UploadUtils;
import com.hamsterwhat.wechat.websocket.netty.handler.MessageHandler;
import com.hamsterwhat.wechat.websocket.netty.utils.SessionManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Service
public class GroupInfoServiceImpl implements GroupInfoService {

    private final UserInfoMapper userInfoMapper;

    private final GroupInfoMapper groupInfoMapper;

    private final UserContactMapper userContactMapper;

    private final SystemSettings systemSettings;

    private final AppProperties appProperties;

    private final ChatSessionMapper chatSessionMapper;

    private final ChatSessionUserMapper chatSessionUserMapper;

    private final ChatMessageMapper chatMessageMapper;

    private final UserContactService userContactService;

    private final ChatSessionUserService chatSessionUserService;

    private final ChatMessageService chatMessageService;

    private final MessageHandler messageHandler;

    private final RedisUtils redisUtils;

    @Autowired
    public GroupInfoServiceImpl(
            UserInfoMapper userInfoMapper,
            GroupInfoMapper groupInfoMapper,
            UserContactMapper userContactMapper,
            SystemSettings systemSettings,
            AppProperties appProperties,
            ChatSessionMapper chatSessionMapper,
            ChatSessionUserMapper chatSessionUserMapper,
            ChatMessageMapper chatMessageMapper,
            UserContactService userContactService,
            ChatSessionUserService chatSessionUserService,
            ChatMessageService chatMessageService,
            MessageHandler messageHandler,
            RedisUtils redisUtils
    ) {
        this.userInfoMapper = userInfoMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.userContactMapper = userContactMapper;
        this.systemSettings = systemSettings;
        this.appProperties = appProperties;
        this.chatSessionMapper = chatSessionMapper;
        this.chatSessionUserMapper = chatSessionUserMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.userContactService = userContactService;
        this.chatSessionUserService = chatSessionUserService;
        this.chatMessageService = chatMessageService;
        this.messageHandler = messageHandler;
        this.redisUtils = redisUtils;
    }

    @Override
    public GroupInfo getGroupInfoById(String id) {
        return this.groupInfoMapper.selectGroupInfoById(Long.parseLong(id));
    }

    @Override
    public GroupInfo getGroupInfoByGroupId(String groupId) {
        return this.groupInfoMapper.selectGroupInfoByGroupId(groupId);
    }

    @Override
    public List<GroupInfo> findListByParam(GroupInfoQuery param) {
        if (param == null) {
            return this.groupInfoMapper.selectGroupInfoList();
        }
        return this.groupInfoMapper.selectGroupInfoListByQuery(param);
    }

    @Override
    public Integer findCountByParam(GroupInfoQuery param) {
        return this.groupInfoMapper.selectGroupInfoCountByQuery(
                param == null ? new GroupInfoQuery() : param
        );
    }

    @Override
    public PaginationResultVO<GroupInfo> findPageByParam(GroupInfoQuery param) {
        return null;
    }

    @Override
    public Integer add(GroupInfo groupInfo) {
        this.groupInfoMapper.insertGroupInfo(groupInfo);
        return 1;
    }

    @Override
    public Integer addBatch(List<GroupInfo> list) {
        return 0;
    }

    @Override
    public Integer update(GroupInfo groupInfo) {
        this.groupInfoMapper.updateGroupInfo(groupInfo);
        return 1;
    }

    @Override
    public Integer updateBatch(List<GroupInfo> list) {
        return 0;
    }

    @Override
    public Integer addAndUpdateBatch(List<GroupInfo> list) {
        return 0;
    }

    @Override
    public Integer deleteById(Long id) {
        this.groupInfoMapper.deleteGroupInfoById(id);
        return 1;
    }

    @Override
    public Integer deleteByGroupId(String groupId) {
        this.groupInfoMapper.deleteGroupInfoByGroupId(groupId);
        return 1;
    }

    @Override
    public Integer deleteByGroupOwnerId(String groupOwnerId) {
        this.groupInfoMapper.deleteGroupInfoByGroupOwnerId(groupOwnerId);
        return 1;
    }

    @Override
    public Integer deleteGroupInfoByIds(Long[] ids) {
        return 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGroup(GroupInfo groupInfo, MultipartFile groupImg) {
        // Create new group
        GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
        groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
        Integer count = this.groupInfoMapper.selectGroupInfoCountByQuery(groupInfoQuery);
        if (systemSettings.isExceedGroupCountLimit(count)) {
            throw new BusinessException("You have reached maximum groups you can create.");
        }
        if (groupImg == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String groupId = UserContactTypeEnum.GROUP.getPrefix() +
                StringUtils.getRandomNumber(SystemConstants.GROUP_ID_LENGTH);
        groupInfo.setGroupId(groupId);
        this.groupInfoMapper.insertGroupInfo(groupInfo);

        UserContact userContact = new UserContact();
        userContact.setUserId(groupInfo.getGroupOwnerId());
        userContact.setContactorId(groupInfo.getGroupId());
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContact.setContactType(UserContactTypeEnum.GROUP.getType());
        this.userContactMapper.insertUserContact(userContact);

        // Upload group image
        uploadGroupImage(groupInfo, groupImg);

        // Create group session
        Date currentTime = new Date();
        String sessionId = StringUtils.getSessionId(groupId);
        ChatSession chatSession = new ChatSession();
        chatSession.setId(sessionId);
        chatSession.setLastMessage(SystemConstants.DEFAULT_GROUP_CREATE_MSG);
        chatSession.setLastReceiveTime(currentTime);
        this.chatSessionMapper.insertChatSession(chatSession);

        ChatSessionUser sessionUser = new ChatSessionUser();
        sessionUser.setUserId(groupInfo.getGroupOwnerId());
        sessionUser.setContactorId(groupId);
        sessionUser.setSessionId(sessionId);
        sessionUser.setContactorName(groupInfo.getGroupName());
        this.chatSessionUserMapper.insertChatSessionUser(sessionUser);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(CommandTypeEnum.GROUP_CREATE.getType());
        chatMessage.setContent(SystemConstants.DEFAULT_GROUP_CREATE_MSG);
        chatMessage.setSendUserId(groupId);
        chatMessage.setSendUserUsername(groupInfo.getGroupName());
        chatMessage.setSendTime(currentTime);
        chatMessage.setContactorId(groupId);
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
        this.chatMessageMapper.insertChatMessage(chatMessage);

        // Update contact cache
        String contactKey = RedisConstants.USER_CONTACT_KEY + groupInfo.getGroupOwnerId();
        this.redisUtils.addToSet(contactKey, RedisConstants.USER_CONTACT_EXPIRE, groupId);

        // Add owner to group channel
        SessionManager.bindChannelGroup(groupId, groupInfoQuery.getGroupOwnerId());

        // Create and send message
        MessageDTO<? extends Serializable> messageDTO = new MessageDTO<>();
        BeanUtils.copyProperties(chatMessage, messageDTO);
        messageDTO.setContactorName(groupInfo.getGroupName());
        this.messageHandler.sendMessage(messageDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGroup(GroupInfo groupInfo, MultipartFile groupImg) {
        // Update existing group
        GroupInfo dbGroupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(groupInfo.getGroupId());
        if (dbGroupInfo == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!dbGroupInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        this.groupInfoMapper.updateGroupInfo(groupInfo);

        // Upload group image
        uploadGroupImage(groupInfo, groupImg);

        if (dbGroupInfo.getGroupName().equals(groupInfo.getGroupName())) {
            return;
        }

        // Update tables related to groupName
        this.chatSessionUserService.updateContactorName(groupInfo.getGroupId(), groupInfo.getGroupName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deactivateGroup(String userId, String groupId) {
        GroupInfo groupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(groupId);
        if (groupInfo == null || !groupInfo.getGroupOwnerId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // Deactivate group
        GroupInfo updateGroupInfo = new GroupInfo();
        updateGroupInfo.setGroupId(groupId);
        updateGroupInfo.setStatus(GroupStatusEnum.INACTIVE.getStatus());
        this.groupInfoMapper.updateGroupInfo(updateGroupInfo);

        // Delete all related relationships
        UserContactQuery query = new UserContactQuery();
        query.setContactorId(groupId);
        query.setContactType(UserContactTypeEnum.GROUP.getType());
        UserContact userContact = new UserContact();
        userContact.setStatus(UserContactStatusEnum.DELETED.getStatus());
        this.userContactMapper.updateUserContactByQuery(userContact, query);

        // Delete cache
        this.userContactMapper.selectUserContactListByQuery(query)
                .forEach(groupMember -> {
                    String memberId = groupMember.getUserId();
                    String contactKey = RedisConstants.USER_CONTACT_KEY + memberId;
                    this.redisUtils.removeFromSet(contactKey, groupId);
                });

        // Send message
        String content = SystemConstants.DEFAULT_GROUP_DEACTIVATE_MSG;
        this.chatMessageService.sendGroupNotificationMessage(
                groupId, CommandTypeEnum.GROUP_DEACTIVATE.getType(), content, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(String userId, String groupId) {
        GroupInfo groupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(groupId);
        UserInfo userInfo = this.userInfoMapper.selectUserInfoByUserId(userId);
        if (groupInfo == null || userInfo == null || groupInfo.getGroupOwnerId().equals(userId)) {
            // the owner of the group cannot leave the group
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // Delete relationship
        Integer count = this.userContactService.deleteContact(userId, groupId, false);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String content = String.format(SystemConstants.DEFAULT_LEAVE_GROUP_MSG, userInfo.getUsername());
        this.chatMessageService.sendGroupNotificationMessage(
                groupId, CommandTypeEnum.LEAVE_GROUP.getType(), content, userId
        );
    }

    @Override
    public GroupInfo getGroupInfo(String userId, String groupId) {
        UserContact userContact = this.userContactMapper.selectUserContactByPrimaryKeys(
                userId, groupId
        );
        if (userContact == null ||
                !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException("You are not in this group!");
        }

        GroupInfo groupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(groupId);
        if (groupInfo == null || GroupStatusEnum.INACTIVE.getStatus().equals(groupInfo.getStatus())) {
            throw new BusinessException("The group doesn't exist!");
        }
        return groupInfo;
    }

    @Override
    public void addGroupMember(String userId, String groupId, String memberId) {
        GroupInfo groupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(groupId);
        if (groupInfo == null || !groupInfo.getGroupOwnerId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        this.userContactService.addContact(
                memberId, userId, groupId, UserContactTypeEnum.GROUP.getType(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroupMember(String userId, String groupId, String memberId) {
        if (userId.equals(memberId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        GroupInfo groupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(groupId);
        UserInfo memberUserInfo = this.userInfoMapper.selectUserInfoByUserId(memberId);
        if (groupInfo == null || memberUserInfo == null || !groupInfo.getGroupOwnerId().equals(userId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // Delete relationship
        Integer count = this.userContactService.deleteContact(memberId, groupId, false);
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String content = String.format(SystemConstants.DEFAULT_REMOVE_FROM_GROUP_MSG, memberUserInfo.getUsername());
        this.chatMessageService.sendGroupNotificationMessage(
                groupId, CommandTypeEnum.LEAVE_GROUP.getType(), content, memberId
        );
    }

    private void uploadGroupImage(GroupInfo groupInfo, MultipartFile groupImg) {
        if (groupImg == null) {
            return;
        }
        String avatarFolderPath = this.appProperties.getProject().getFolder() +
                SystemConstants.UPLOAD_AVATAR_FOLDER;
        UploadUtils.uploadAvatar(avatarFolderPath, groupInfo.getGroupId(), groupImg);
    }
}
