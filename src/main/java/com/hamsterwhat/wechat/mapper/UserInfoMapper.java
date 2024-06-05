package com.hamsterwhat.wechat.mapper;

import com.hamsterwhat.wechat.entity.po.UserInfo;
import com.hamsterwhat.wechat.entity.query.UserInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserInfoMapper {
    UserInfo selectUserInfoById(Long id);

    UserInfo selectUserInfoByUserId(String userId);

    UserInfo selectUserInfoByEmail(String email);

    List<UserInfo> selectUserInfoList();

    List<UserInfo> selectUserInfoListByQuery(@Param("query") UserInfoQuery query);

    Integer selectUserInfoCountByQuery(@Param("query") UserInfoQuery query);

    Integer insertUserInfo(UserInfo userInfo);

    Integer updateUserInfo(UserInfo userInfo);

    Integer deleteUserInfoById(Long id);

    Integer deleteUserInfoByUserId(String userId);

    Integer deleteUserInfoByEmail(String email);

    Integer deleteUserInfoByIds(@Param("ids") Long[] ids);

    Integer deleteUserInfoByQuery(@Param("query") UserInfoQuery query);
}
