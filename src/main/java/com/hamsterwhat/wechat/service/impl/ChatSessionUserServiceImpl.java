package com.hamsterwhat.wechat.service.impl;

import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.enums.CommandTypeEnum;
import com.hamsterwhat.wechat.entity.enums.UserContactStatusEnum;
import com.hamsterwhat.wechat.entity.enums.UserContactTypeEnum;
import com.hamsterwhat.wechat.entity.po.ChatSessionUser;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;
import com.hamsterwhat.wechat.mapper.ChatSessionUserMapper;
import com.hamsterwhat.wechat.mapper.UserContactMapper;
import com.hamsterwhat.wechat.service.ChatSessionUserService;
import com.hamsterwhat.wechat.websocket.netty.handler.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatSessionUserServiceImpl implements ChatSessionUserService {

    private final UserContactMapper userContactMapper;

    private final ChatSessionUserMapper chatSessionUserMapper;

    private final MessageHandler messageHandler;

    @Autowired
    public ChatSessionUserServiceImpl(
            UserContactMapper userContactMapper,
            ChatSessionUserMapper chatSessionUserMapper,
            MessageHandler messageHandler
    ) {
        this.userContactMapper = userContactMapper;
        this.chatSessionUserMapper = chatSessionUserMapper;
        this.messageHandler = messageHandler;
    }

    @Override
    public void updateContactorName(String contactorId, String contactorName) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum
                .getByPrefix(contactorId.substring(0, 1));

        // update username in chat sessions
        ChatSessionUser sessionUser = new ChatSessionUser();
        sessionUser.setContactorId(contactorId);
        sessionUser.setContactorName(contactorName);
        this.chatSessionUserMapper.updateChatSessionUserByContactorId(sessionUser);

        // Send ws notice for update
        if (contactTypeEnum == UserContactTypeEnum.GROUP) {
            MessageDTO<String> messageDTO = new MessageDTO<>();
            messageDTO.setMessageType(CommandTypeEnum.CONTACT_INFO_UPDATE.getType());
            messageDTO.setContactorId(contactorId);
            messageDTO.setContactType(contactTypeEnum.getType());
            messageDTO.setExtendData(contactorName);
            this.messageHandler.sendMessage(messageDTO);
        } else if (contactTypeEnum == UserContactTypeEnum.USER) {
            UserContactQuery query = new UserContactQuery();
            query.setContactorId(contactorId);
            query.setContactType(UserContactTypeEnum.USER.getType());
            query.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            this.userContactMapper
                    .selectUserContactListByQuery(query)
                    .forEach(userContact -> {
                        MessageDTO<String> messageDTO = new MessageDTO<>();
                        messageDTO.setMessageType(CommandTypeEnum.CONTACT_INFO_UPDATE.getType());
                        messageDTO.setSendUserId(contactorId);
                        messageDTO.setContactorId(userContact.getUserId());
                        messageDTO.setContactType(UserContactTypeEnum.USER.getType());
                        messageDTO.setExtendData(contactorName);
                        this.messageHandler.sendMessage(messageDTO);
                    });
        }

    }
}
