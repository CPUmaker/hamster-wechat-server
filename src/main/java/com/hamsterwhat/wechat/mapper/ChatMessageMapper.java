package com.hamsterwhat.wechat.mapper;

import com.hamsterwhat.wechat.entity.po.ChatMessage;
import com.hamsterwhat.wechat.entity.query.ChatMessageQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatMessageMapper {
    ChatMessage selectChatMessageById(Long id);

    List<ChatMessage> selectChatMessageList();

    List<ChatMessage> selectChatMessageListBySessionId(String sessionId);

    List<ChatMessage> selectChatMessageListByQuery(@Param("query") ChatMessageQuery query);

    Integer selectChatMessageCountByQuery(@Param("query") ChatMessageQuery query);

    Integer insertChatMessage(ChatMessage chatMessage);

    Integer updateChatMessage(ChatMessage chatMessage);

    Integer deleteChatMessageById(Long id);

    Integer deleteChatMessageBySessionId(String sessionId);

    Integer deleteChatMessageByQuery(@Param("query") ChatMessageQuery query);
}
