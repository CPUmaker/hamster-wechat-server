package com.hamsterwhat.wechat.websocket.netty.utils;

import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.enums.CommandTypeEnum;
import com.hamsterwhat.wechat.entity.enums.UserContactTypeEnum;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.utils.JsonUtils;
import com.hamsterwhat.wechat.utils.StringUtils;
import com.hamsterwhat.wechat.websocket.netty.attribute.Attributes;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SessionManager {

    private static final ConcurrentMap<String, Channel> userChannelMap = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, ChannelGroup> groupChannelMap = new ConcurrentHashMap<>();

    public static Channel getChannel(String userId) {
        return userChannelMap.get(userId);
    }

    public static void bindChannel(String userId, Channel channel) {
        userChannelMap.put(userId, channel);
    }

    public static void unBindChannel(String userId) {
        userChannelMap.remove(userId);
    }

    public static void sendChannel(MessageDTO<?> message, String receiveUserId) {
        if (StringUtils.isEmpty(receiveUserId)) {
            return;
        }
        Channel channel = userChannelMap.get(receiveUserId);
        if (channel == null) {
            return;
        }
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.toJson(message)));
    }

    public static void bindChannelGroup(String groupId, String userId) {
        if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("groupId and userId can't be null");
        }
        Channel userChannel = getChannel(userId);
        if (userChannel == null) {
            throw new BusinessException("The user has not registered yet!");
        }
        bindChannelGroup(groupId, userChannel);
    }

    public static void bindChannelGroup(String groupId, Channel channel) {
        ChannelGroup channelGroup = groupChannelMap.computeIfAbsent(groupId, key -> {
            ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            groupChannelMap.put(groupId, group);
            return group;
        });
        channelGroup.add(channel);
    }

    public static void unbindChannelGroup(String groupId, String userId) {
        ChannelGroup channelGroup = groupChannelMap.get(groupId);
        if (channelGroup == null) {
            throw new BusinessException("The group has not registered yet!");
        }
        Channel userChannel = getChannel(userId);
        if (userChannel == null) {
            throw new BusinessException("The user has not registered yet!");
        }
        channelGroup.remove(userChannel);
    }

    public static void sendChannelGroup(MessageDTO<?> message, String receiveGroupId) {
        if (StringUtils.isEmpty(receiveGroupId)) {
            return;
        }
        ChannelGroup channelGroup = groupChannelMap.get(receiveGroupId);
        if (channelGroup == null) {
            return;
        }
        channelGroup.writeAndFlush(new TextWebSocketFrame(JsonUtils.toJson(message)));
    }

    public static ChannelGroup getChannelGroup(String groupId) {
        return groupChannelMap.get(groupId);
    }

    public static void sendMessage(MessageDTO<?> messageDTO) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByType(messageDTO.getContactType());
        if (contactTypeEnum == UserContactTypeEnum.USER) {
            sendMessageToUser(messageDTO);
        } else if (contactTypeEnum == UserContactTypeEnum.GROUP) {
            sendMessageToGroup(messageDTO);
        }
    }

    private static void sendMessageToUser(MessageDTO<?> messageDTO) {
        String contactorId = messageDTO.getContactorId();
        sendChannel(messageDTO, contactorId);
        if (CommandTypeEnum.ADD_FRIEND.getType().equals(messageDTO.getMessageType())) {
            String sendUserId = messageDTO.getSendUserId();
            sendChannel(messageDTO, sendUserId);
        }

        // force offline
        if (CommandTypeEnum.FORCE_OFFLINE.getType().equals(messageDTO.getMessageType())) {
            Channel channel = userChannelMap.get(contactorId);
            if (channel != null) {
                channel.attr(Attributes.FORCE_OFFLINE).set(Boolean.TRUE);
                channel.close();
            }
        }
    }

    private static void sendMessageToGroup(MessageDTO<?> messageDTO) {
        String groupId = messageDTO.getContactorId();
        sendChannelGroup(messageDTO, groupId);

        CommandTypeEnum commandType = CommandTypeEnum.getByType(messageDTO.getMessageType());
        if (commandType == CommandTypeEnum.LEAVE_GROUP ||
                commandType == CommandTypeEnum.GROUP_REMOVE_MEMBER) {
            String leavingUserId = (String) messageDTO.getExtendData();
            Channel channel = userChannelMap.get(leavingUserId);
            if (channel != null) {
                channel.pipeline().fireUserEventTriggered(new GroupMemberRemoval(groupId, leavingUserId));
            }
        }
        if (commandType == CommandTypeEnum.GROUP_DEACTIVATE) {
            ChannelGroup group = groupChannelMap.remove(groupId);
            if (group != null) {
                group.close();
            }
        }
    }

    @Getter
    public static class GroupMemberRemoval {

        private final String groupId;

        private final String memberId;

        public GroupMemberRemoval(String groupId, String memberId) {
            this.groupId = groupId;
            this.memberId = memberId;
        }
    }

    private SessionManager() {}
}
