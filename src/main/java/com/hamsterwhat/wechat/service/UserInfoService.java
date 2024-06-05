package com.hamsterwhat.wechat.service;

import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.po.UserInfo;
import com.hamsterwhat.wechat.entity.query.UserInfoQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import com.hamsterwhat.wechat.entity.vo.UserInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserInfoService {

    UserInfo getUserInfoById(String id);

    UserInfo getUserInfoByUserId(String userId);

    UserInfo getUserInfoByEmail(String email);

    List<UserInfo> findListByParam(UserInfoQuery param);

    Integer findCountByParam(UserInfoQuery param);

    PaginationResultVO<UserInfo> findPageByParam(UserInfoQuery param);

    Integer add(UserInfo userInfo);

    Integer addBatch(List<UserInfo> list);

    Integer update(UserInfo userInfo);

    Integer updateBatch(List<UserInfo> list);

    Integer addAndUpdateBatch(List<UserInfo> list);

    Integer deleteById(Long id);

    Integer deleteUserInfoByIds(Long[] ids);

    /**
     * Register for new account
     * @param email new account's email
     * @param password new account's password
     * @param username new account's username
     */
    void register(String email, String password, String username);

    /**
     * Login for users
     * @param email user's email
     * @param password user's password
     * @return user info including token digest
     */
    UserInfoVO login(String email, String password);

    void logout(String userId);

    void resetPassword(String userId, String originPassword, String newPassword);

    void updateUserInfo(UserInfo userInfo, MultipartFile avatar, TokenUserInfoDTO token);
}
