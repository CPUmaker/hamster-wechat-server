package com.hamsterwhat.wechat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hamsterwhat.wechat.entity.enums.UserContactStatusEnum;
import com.hamsterwhat.wechat.entity.po.UserContact;
import com.hamsterwhat.wechat.entity.query.BaseParam;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;
import com.hamsterwhat.wechat.entity.query.UserInfoQuery;
import com.hamsterwhat.wechat.mapper.UserContactMapper;
import com.hamsterwhat.wechat.mapper.UserInfoMapper;
import com.hamsterwhat.wechat.entity.po.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootTest
class HamsterWechatServerApplicationTests {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    UserContactMapper userContactMapper;

    @Test
    void testUserInfoMapperInsert() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId("U11111111111");
        userInfo.setUsername("Chris Li");
        userInfo.setEmail("1070743423@qq.com");
        userInfo.setLastLoginTime(new Date());
        userInfoMapper.insertUserInfo(userInfo);
    }

    @Test
    void testUserInfoMapperQuery() {
        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setUsernameLike("Chris");
        userInfoQuery.setLastLoginTimeEnd(new Date());
        List<UserInfo> userInfos = userInfoMapper.selectUserInfoListByQuery(userInfoQuery);
        System.out.println(userInfos);
    }

    @Test
    void testUserContactMapperUpdate() {
        UserContact userContact = new UserContact();
        userContact.setCreatedAt(new Date());
        userContactMapper.updateUserContact(userContact);
    }

    @Test
    void testUserContactMapperQuery() {
        UserContactQuery userContactQuery = new UserContactQuery();
        userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContactQuery.setIncludesUser(BaseParam.TableJoinType.INNER);

        List<UserContact> list = userContactMapper.selectUserContactListByQuery(userContactQuery);
        System.out.println(list);
    }
}
