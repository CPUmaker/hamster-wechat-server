package com.hamsterwhat.wechat.mapper;

import com.hamsterwhat.wechat.entity.po.ChatSessionUser;
import com.hamsterwhat.wechat.entity.query.ChatSessionUserQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatSessionUserMapper {
    ChatSessionUser selectChatSessionUserByPrimaryKeys(
            @Param("userId") String userId, @Param("contactorId") String contactorId);

    ChatSessionUser selectChatSessionUserBySessionId(String sessionId);

    List<ChatSessionUser> selectChatSessionUserList();

    List<ChatSessionUser> selectChatSessionUserListByUserId(String userId);

    List<ChatSessionUser> selectChatSessionUserListByQuery(@Param("query") ChatSessionUserQuery query);

    Integer selectChatSessionUserCountByQuery(@Param("query") ChatSessionUserQuery query);

    Integer insertChatSessionUser(ChatSessionUser chatSessionUser);

    Integer updateChatSessionUser(ChatSessionUser chatSessionUser);

    Integer updateChatSessionUserByContactorId(ChatSessionUser chatSessionUser);

    Integer insertOrUpdateChatSessionUser(ChatSessionUser chatSessionUser);

    Integer deleteChatSessionUserByPrimaryKeys(
            @Param("userId") String userId, @Param("contactorId") String contactorId);

    Integer deleteChatSessionUserByQuery(@Param("query") ChatSessionUserQuery query);
}
