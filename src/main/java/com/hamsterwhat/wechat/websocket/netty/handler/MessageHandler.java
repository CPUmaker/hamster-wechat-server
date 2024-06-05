package com.hamsterwhat.wechat.websocket.netty.handler;

import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.enums.CommandTypeEnum;
import com.hamsterwhat.wechat.entity.enums.UserContactTypeEnum;
import com.hamsterwhat.wechat.websocket.netty.utils.SessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private static final String MESSAGE_TOPIC = "message";

    @Resource
    private RedissonClient redissonClient;

    @PostConstruct
    public void listenOnMessage() {
        RTopic rTopic = this.redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageDTO.class, (charSequence, messageDTO) -> SessionManager.sendMessage(messageDTO));
    }

    public void sendMessage(MessageDTO<?> message) {
        RTopic rTopic = this.redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(message);
    }

    public void sendForceOfflineMessage(String contactorId) {
        UserContactTypeEnum contactType = UserContactTypeEnum.getByPrefix(contactorId.substring(0, 1));
        if (contactType == UserContactTypeEnum.USER) {
            MessageDTO<? extends Serializable> message = new MessageDTO<>();
            message.setMessageType(CommandTypeEnum.FORCE_OFFLINE.getType());
            message.setContactorId(contactorId);
            message.setContactType(contactType.getType());
            sendMessage(message);
            logger.info("User {} has been forced to go offline successfully", contactorId);
        }
    }
}
