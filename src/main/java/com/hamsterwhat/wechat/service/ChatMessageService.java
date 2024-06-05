package com.hamsterwhat.wechat.service;

import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.po.ChatMessage;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.nio.file.Path;

public interface ChatMessageService {

    ChatMessage getChatMessageById(Long messageId);

    MessageDTO<Serializable> saveMessage(ChatMessage chatMessage, TokenUserInfoDTO token);

    <T extends Serializable> void sendGroupNotificationMessage(
            String groupId, Short messageType, String content, T extendData);

    void uploadFile(String userId, Long messageId, MultipartFile file);

    Path getMessageFilePath(String userId, Long messageId, Path filePath, boolean showCover);
}
