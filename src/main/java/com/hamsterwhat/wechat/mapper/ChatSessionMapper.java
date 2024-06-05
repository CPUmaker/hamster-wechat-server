package com.hamsterwhat.wechat.mapper;

import com.hamsterwhat.wechat.entity.po.ChatSession;
import com.hamsterwhat.wechat.entity.query.ChatSessionQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatSessionMapper {
    ChatSession selectChatSessionById(String id);

    List<ChatSession> selectChatSessionList();

    List<ChatSession> selectChatSessionListByIds(@Param("ids") List<String> ids);

    List<ChatSession> selectChatSessionListByQuery(@Param("query") ChatSessionQuery query);

    Integer selectChatSessionCountByQuery(@Param("query") ChatSessionQuery query);

    Integer insertChatSession(ChatSession chatSession);

    Integer updateChatSession(ChatSession chatSession);

    Integer insertOrUpdateChatSession(ChatSession chatSession);

    Integer deleteChatSessionById(String id);
}
