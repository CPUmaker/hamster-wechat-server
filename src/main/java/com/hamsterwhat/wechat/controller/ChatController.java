package com.hamsterwhat.wechat.controller;


import com.hamsterwhat.wechat.annotation.GlobalInterceptor;
import com.hamsterwhat.wechat.entity.constants.AppProperties;
import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.enums.CommandTypeEnum;
import com.hamsterwhat.wechat.entity.enums.FileTypeEnum;
import com.hamsterwhat.wechat.entity.enums.MessageStatusEnum;
import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.entity.po.ChatMessage;
import com.hamsterwhat.wechat.entity.vo.ResponseVO;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.service.ChatMessageService;
import com.hamsterwhat.wechat.utils.DownloadUtils;
import com.hamsterwhat.wechat.utils.TokenUserInfoHolder;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/chat")
public class ChatController extends BaseController {

    private final ChatMessageService chatMessageService;

    private final AppProperties appProperties;

    @Autowired
    public ChatController(
            ChatMessageService chatMessageService,
            AppProperties appProperties
    ) {
        this.chatMessageService = chatMessageService;
        this.appProperties = appProperties;
    }

    @PostMapping("/message")
    @GlobalInterceptor
    public ResponseVO<Object> sendMessage(
            @NotEmpty String contactorId,
            @NotEmpty @Max(500) String content,
            @NotNull Short messageType,
            Long fileSize,
            String fileName,
            Short fileType
    ) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        CommandTypeEnum commandTypeEnum = CommandTypeEnum.getByType(messageType);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessageType(messageType);
        chatMessage.setContent(content);
        chatMessage.setContactorId(contactorId);

        MessageDTO<? extends Serializable> messageDTO = null;
        switch (commandTypeEnum) {
            case CHAT_MESSAGE -> {
                chatMessage.setStatus(MessageStatusEnum.SENT.getStatus());
                messageDTO = this.chatMessageService.saveMessage(chatMessage, token);
            }
            case MEDIA_CHAT -> {
                if (fileSize == null || fileName == null || fileType == null) {
                    throw new BusinessException(ResponseCodeEnum.CODE_600);
                }
                chatMessage.setStatus(MessageStatusEnum.SENDING.getStatus());
                chatMessage.setFileSize(fileSize);
                chatMessage.setFileName(fileName);
                chatMessage.setFileType(fileType);
                messageDTO = this.chatMessageService.saveMessage(chatMessage, token);
            }
            default -> {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
        return getSuccessResponse(messageDTO);
    }

    @PostMapping("/upload")
    @GlobalInterceptor
    public ResponseVO<Object> upload(
            @NotNull Long messageId,
            @NotNull MultipartFile file
    ) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        this.chatMessageService.uploadFile(token.getUserId(), messageId, file);
        return getSuccessResponse(null);
    }

    @GetMapping("/download")
    @GlobalInterceptor
    public void download(
            HttpServletResponse response,
            @NotEmpty String fileId,
            @NotNull Short fileType,
            @NotNull Boolean showCover
    ) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        String userId = token.getUserId();
        FileTypeEnum fileTypeEnum = FileTypeEnum.getByType(fileType);

        Path projectPath = Paths.get(this.appProperties.getProject().getFolder());
        Path filePath;
        switch (fileTypeEnum) {
            case AVATAR -> {
                String fileName = fileId + (Boolean.TRUE.equals(showCover) ? SystemConstants.COVER_SUFFIX : "");
                filePath = projectPath.resolve(SystemConstants.UPLOAD_AVATAR_FOLDER)
                        .resolve(fileName);
            }
            case RELEASE_PACKAGE -> {
                filePath = projectPath.resolve(SystemConstants.UPLOAD_RELEASE_FOLDER)
                        .resolve(fileId + SystemConstants.RELEASE_FILE_EXTENSION);
            }
            case IMAGE -> {
                Path imageFolder = projectPath.resolve(SystemConstants.UPLOAD_IMAGE_FOLDER);
                filePath = this.chatMessageService.getMessageFilePath(
                        userId, Long.valueOf(fileId), imageFolder, showCover);
            }
            case VIDEO -> {
                Path videoFolder = projectPath.resolve(SystemConstants.UPLOAD_VIDEO_FOLDER);
                filePath = this.chatMessageService.getMessageFilePath(
                        userId, Long.valueOf(fileId), videoFolder, showCover);
            }
            case NORMAL_FILE -> {
                Path fileFolder = projectPath.resolve(SystemConstants.UPLOAD_FILE_FOLDER);
                filePath = this.chatMessageService.getMessageFilePath(
                        userId, Long.valueOf(fileId), fileFolder, showCover);
            }
            default -> {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }

        filePath = filePath.normalize();
        DownloadUtils.downloadFile(filePath, response);
    }
}
