package com.hamsterwhat.wechat.mapper;

import com.hamsterwhat.wechat.entity.po.UserContact;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserContactMapper {
    UserContact selectUserContactByPrimaryKeys(@Param("userId") String userId,
                                               @Param("contactorId") String contactorId);

    List<UserContact> selectUserContactList();

    List<UserContact> selectUserContactListByUserId(String userId);

    List<UserContact> selectUserContactListByContactorId(String contactorId);

    List<UserContact> selectUserContactListByQuery(@Param("query") UserContactQuery query);

    Integer selectUserContactCountByQuery(@Param("query") UserContactQuery query);

    Integer insertUserContact(UserContact userInfo);

    Integer updateUserContact(UserContact userInfo);

    Integer updateUserContactByQuery(
            @Param("userInfo") UserContact userInfo,
            @Param("query") UserContactQuery query
    );

    Integer insertOrUpdateUserContact(UserContact userInfo);

    Integer deleteUserContactByPrimaryKeys(@Param("userId") String userId,
                                        @Param("contactorId") String contactorId);

    Integer deleteUserContactByQuery(@Param("query") UserContactQuery query);
}
