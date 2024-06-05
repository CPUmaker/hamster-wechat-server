package com.hamsterwhat.wechat.service.impl;

import com.hamsterwhat.wechat.entity.constants.AppProperties;
import com.hamsterwhat.wechat.entity.constants.RedisConstants;
import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.enums.CommandTypeEnum;
import com.hamsterwhat.wechat.entity.enums.UserContactStatusEnum;
import com.hamsterwhat.wechat.entity.enums.UserContactTypeEnum;
import com.hamsterwhat.wechat.entity.enums.UserStatusEnum;
import com.hamsterwhat.wechat.entity.po.ChatSessionUser;
import com.hamsterwhat.wechat.entity.po.GroupInfo;
import com.hamsterwhat.wechat.entity.po.UserContact;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;
import com.hamsterwhat.wechat.entity.query.UserInfoQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import com.hamsterwhat.wechat.entity.vo.UserInfoVO;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.mapper.ChatSessionUserMapper;
import com.hamsterwhat.wechat.mapper.UserContactMapper;
import com.hamsterwhat.wechat.mapper.UserInfoMapper;
import com.hamsterwhat.wechat.entity.po.UserInfo;
import com.hamsterwhat.wechat.service.ChatSessionUserService;
import com.hamsterwhat.wechat.service.UserContactService;
import com.hamsterwhat.wechat.service.UserInfoService;
import com.hamsterwhat.wechat.utils.CopyUtils;
import com.hamsterwhat.wechat.utils.RedisUtils;
import com.hamsterwhat.wechat.utils.StringUtils;
import com.hamsterwhat.wechat.utils.UploadUtils;
import com.hamsterwhat.wechat.websocket.netty.handler.MessageHandler;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    private final UserInfoMapper userInfoMapper;

    private final UserContactMapper userContactMapper;

    private final UserContactService userContactService;

    private final ChatSessionUserService chatSessionUserService;

    private final MessageHandler messageHandler;

    private final RedisUtils redisUtils;

    private final AppProperties appProperties;

    @Autowired
    UserInfoServiceImpl(
            UserInfoMapper userInfoMapper,
            UserContactMapper userContactMapper,
            UserContactService userContactService,
            ChatSessionUserService chatSessionUserService,
            MessageHandler messageHandler,
            RedisUtils redisUtils,
            AppProperties appProperties
    ) {
        this.userInfoMapper = userInfoMapper;
        this.userContactMapper = userContactMapper;
        this.userContactService = userContactService;
        this.chatSessionUserService = chatSessionUserService;
        this.messageHandler = messageHandler;
        this.redisUtils = redisUtils;
        this.appProperties = appProperties;
    }

    @Override
    public UserInfo getUserInfoById(String id) {
        return userInfoMapper.selectUserInfoById(Long.parseLong(id));
    }

    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return userInfoMapper.selectUserInfoByUserId(userId);
    }

    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return userInfoMapper.selectUserInfoByEmail(email);
    }

    @Override
    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return List.of();
    }

    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return 0;
    }

    @Override
    public PaginationResultVO<UserInfo> findPageByParam(UserInfoQuery param) {
        return null;
    }

    @Override
    public Integer add(UserInfo userInfo) {
        return 0;
    }

    @Override
    public Integer addBatch(List<UserInfo> list) {
        return 0;
    }

    @Override
    public Integer update(UserInfo userInfo) {
        try {
            userInfoMapper.updateUserInfo(userInfo);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Integer updateBatch(List<UserInfo> list) {
        return 0;
    }

    @Override
    public Integer addAndUpdateBatch(List<UserInfo> list) {
        return 0;
    }

    @Override
    public Integer deleteById(Long id) {
        try {
            userInfoMapper.deleteUserInfoById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Integer deleteUserInfoByIds(Long[] ids) {
        try {
            userInfoMapper.deleteUserInfoByIds(ids);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void register(String email, String password, String username) {
        UserInfo userInfo = this.userInfoMapper.selectUserInfoByEmail(email);
        if (userInfo != null) {
            throw new BusinessException("Email address has been used!");
        }

        String userId = UserContactTypeEnum.USER.getPrefix() +
                StringUtils.getRandomNumber(SystemConstants.USER_ID_LENGTH);
        String encodedPassword = StringUtils.encodeMD5(password);
        Date now = new Date();

        UserInfo newUser = new UserInfo();
        newUser.setUserId(userId);
        newUser.setEmail(email);
        newUser.setPassword(encodedPassword);
        newUser.setUsername(username);
        newUser.setStatus(UserStatusEnum.ENABLED.getStatus());
        newUser.setLastOfflineTime(now);
        newUser.setCreatedAt(now);

        this.userInfoMapper.insertUserInfo(newUser);

        // Create robot contact
        this.userContactService.addRobotContact(userId);
    }

    @Override
    public UserInfoVO login(String email, String password) {
        UserInfo userInfo = this.userInfoMapper.selectUserInfoByEmail(email);
        if (userInfo == null) {
            throw new BusinessException("Credential does not exist or is invalid!");
        }

        String encodedPassword = StringUtils.encodeMD5(password);
        if (!encodedPassword.equals(userInfo.getPassword())) {
            throw new BusinessException("Credential does not exist or is invalid!");
        }

        if (userInfo.getStatus().equals(UserStatusEnum.DISABLED.getStatus())) {
            throw new BusinessException("This account is banned!");
        }

        // Check heartbeat
        String userId = userInfo.getUserId();
        String key = RedisConstants.WS_USER_HEARTBEAT_KEY + userId;
        Long lastHeartBeatTime = (Long) this.redisUtils.get(key);

        if (lastHeartBeatTime != null) {
            throw new BusinessException("This account has been logged in!");
        }

        // Generate token
        String digest = StringUtils.encodeMD5(userId +
                StringUtils.getRandomNumber(SystemConstants.TOKEN_DIGEST_LENGTH));
        TokenUserInfoDTO token = new TokenUserInfoDTO();
        token.setUserId(userId);
        token.setUsername(userInfo.getUsername());
        token.setIsAdmin(userInfo.getIsAdmin());
        token.setToken(digest);

        // Save token to Redis
        String tokenKey = RedisConstants.WS_TOKEN_KEY + digest;
        String digestKey = RedisConstants.WS_TOKEN_DIGEST_KEY + userId;
        this.redisUtils.set(tokenKey, token, RedisConstants.WS_TOKEN_EXPIRE);
        this.redisUtils.set(digestKey, digest, RedisConstants.WS_TOKEN_DIGEST_EXPIRE);

        // Query all contacts and update Redis
        UserContactQuery query = new UserContactQuery();
        query.setUserId(userId);
        query.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<Object> contactorIds = this.userContactMapper.selectUserContactListByQuery(query)
                .stream()
                .map(userContact -> (Object) userContact.getContactorId())
                .toList();
        String contactKey = RedisConstants.USER_CONTACT_KEY + userId;
        redisUtils.delete(contactKey);
        redisUtils.addToSet(contactKey, RedisConstants.USER_CONTACT_EXPIRE, contactorIds);

        // Create VO
        UserInfoVO userInfoVO = CopyUtils.copy(userInfo, UserInfoVO.class);
        userInfoVO.setToken(token.getToken());

        return userInfoVO;
    }

    @Override
    public void logout(String userId) {
        // force offline
        this.messageHandler.sendForceOfflineMessage(userId);
    }

    @Override
    public void resetPassword(String userId, String originPassword, String newPassword) {
        UserInfo userInfo = this.userInfoMapper.selectUserInfoByUserId(userId);
        String encodedPassword = StringUtils.encodeMD5(originPassword);
        if (!encodedPassword.equals(userInfo.getPassword())) {
            throw new BusinessException("Credential does not exist or is invalid!");
        }
        encodedPassword = StringUtils.encodeMD5(newPassword);
        UserInfo updateUser = new UserInfo();
        updateUser.setUserId(userId);
        updateUser.setPassword(encodedPassword);
        this.userInfoMapper.updateUserInfo(updateUser);

        // Force offline
        this.messageHandler.sendForceOfflineMessage(userId);
    }

    @Override
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatar, TokenUserInfoDTO token) {
        // Upload avatar
        if (avatar != null) {
            String avatarFolderPath = this.appProperties.getProject().getFolder() +
                    SystemConstants.UPLOAD_AVATAR_FOLDER;
            UploadUtils.uploadAvatar(avatarFolderPath, userInfo.getUserId(), avatar);
        }

        // Update user info
        this.userInfoMapper.updateUserInfo(userInfo);

        UserInfo dbUserInfo = this.userInfoMapper.selectUserInfoByUserId(userInfo.getUserId());
        if (dbUserInfo.getUsername().equals(userInfo.getUsername())) {
            return;
        }

        // update username in chat sessions
        this.chatSessionUserService.updateContactorName(userInfo.getUserId(), userInfo.getUsername());

        // update username in redis token
        String tokenKey = RedisConstants.WS_TOKEN_KEY + token.getToken();
        token.setUsername(userInfo.getUsername());
        this.redisUtils.set(tokenKey, token, RedisConstants.WS_TOKEN_EXPIRE);
    }
}
