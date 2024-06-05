package com.hamsterwhat.wechat.service;

import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.po.ChatMessage;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface ChatMessageService {

    ChatMessage getChatMessageById(Long messageId);

    MessageDTO<Object> saveMessage(ChatMessage chatMessage, TokenUserInfoDTO token);

    void sendGroupNotificationMessage(
            String groupId, Short messageType, String content, Object extendData);

    void uploadFile(String userId, Long messageId, MultipartFile file);

    Path getMessageFilePath(String userId, Long messageId, Path filePath, boolean showCover);
}
