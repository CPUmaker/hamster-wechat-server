package com.hamsterwhat.wechat.websocket.netty.handler;

import com.hamsterwhat.wechat.entity.constants.RedisConstants;
import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.dto.WsInitDTO;
import com.hamsterwhat.wechat.entity.enums.CommandTypeEnum;
import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.entity.enums.UserContactApplyStatusEnum;
import com.hamsterwhat.wechat.entity.enums.UserContactTypeEnum;
import com.hamsterwhat.wechat.entity.po.ChatMessage;
import com.hamsterwhat.wechat.entity.po.ChatSessionUser;
import com.hamsterwhat.wechat.entity.po.UserInfo;
import com.hamsterwhat.wechat.entity.query.ChatMessageQuery;
import com.hamsterwhat.wechat.entity.query.UserContactApplyQuery;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.mapper.ChatMessageMapper;
import com.hamsterwhat.wechat.mapper.ChatSessionUserMapper;
import com.hamsterwhat.wechat.mapper.UserContactApplyMapper;
import com.hamsterwhat.wechat.mapper.UserInfoMapper;
import com.hamsterwhat.wechat.utils.RedisUtils;
import com.hamsterwhat.wechat.utils.StringUtils;
import com.hamsterwhat.wechat.websocket.netty.attribute.Attributes;
import com.hamsterwhat.wechat.websocket.netty.utils.SessionManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component
@Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketFrameHandler.class);

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private RedisUtils redisUtils;

    /**
     * Handles incoming WebSocket frames
     * @param ctx The {@link ChannelHandlerContext} of the session
     * @param webSocketFrame The {@link WebSocketFrame} containing the request frame
     * @throws Exception Passes any thrown exceptions up the stack
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame) throws Exception {
        if (webSocketFrame instanceof TextWebSocketFrame) {
            Channel channel = ctx.channel();
            String userId = channel.attr(Attributes.AUTH_USER_ID).get();
            String heartbeatKey = RedisConstants.WS_USER_HEARTBEAT_KEY + userId;
            redisUtils.set(
                    heartbeatKey,
                    Long.valueOf(System.currentTimeMillis()),
                    RedisConstants.WS_USER_HEARTBEAT_EXPIRE
            );
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof HandshakeComplete event) {
            String userId = ctx.channel().attr(Attributes.AUTH_USER_ID).get();
            if (StringUtils.isEmpty(userId)) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }

            // Save heartbeat into Redis
            String heartbeatKey = RedisConstants.WS_USER_HEARTBEAT_KEY + userId;
            redisUtils.set(
                    heartbeatKey,
                    Long.valueOf(System.currentTimeMillis()),
                    RedisConstants.WS_USER_HEARTBEAT_EXPIRE
            );

            // Bind group contacts channel
            String contactKey = RedisConstants.USER_CONTACT_KEY + userId;
            redisUtils.getMembersOfSet(contactKey)
                    .stream()
                    .map(object -> Objects.toString(object, null))
                    .filter(Objects::nonNull)
                    .filter(id -> id.startsWith(UserContactTypeEnum.GROUP.getPrefix()))
                    .forEach(groupId -> SessionManager.bindChannelGroup(groupId, ctx.channel()));

            // Update user info
            UserInfo update = new UserInfo();
            update.setUserId(userId);
            update.setLastLoginTime(new Date());
            this.userInfoMapper.updateUserInfo(update);

            // Send initializing data to user
            WsInitDTO initData = new WsInitDTO();

            // All sessions
            List<ChatSessionUser> sessionUserList = this.chatSessionUserMapper
                    .selectChatSessionUserListByUserId(userId);
            initData.setChatSessionUserList(sessionUserList);

            // All offline messages
            UserInfo userInfo = this.userInfoMapper.selectUserInfoByUserId(userId);
            Date lastOfflineTime = userInfo.getLastOfflineTime();
            List<String> sessionIds = this.chatSessionUserMapper.selectChatSessionUserListByUserId(userId)
                    .stream()
                    .map(ChatSessionUser::getSessionId)
                    .toList();
            ChatMessageQuery query = new ChatMessageQuery();
            query.setSessionIdIn(sessionIds);
            query.setSendTimeStart(lastOfflineTime);
            List<ChatMessage> messageList = this.chatMessageMapper.selectChatMessageListByQuery(query);
            initData.setChatMessageList(messageList);

            // Count of new friend requests
            UserContactApplyQuery applyQuery = new UserContactApplyQuery();
            applyQuery.setReceiveUserId(userId);
            applyQuery.setStatus(UserContactApplyStatusEnum.PENDING.getStatus());
            applyQuery.setLastApplyTimeStart(lastOfflineTime);
            Integer applyCount = this.userContactApplyMapper.selectUserContactApplyCountByQuery(applyQuery);
            initData.setNewApplyCount(applyCount);

            MessageDTO<WsInitDTO> messageDTO = new MessageDTO<>();
            messageDTO.setExtendData(initData);
            messageDTO.setMessageType(CommandTypeEnum.INIT.getType());
            messageDTO.setContactorId(userId);

            SessionManager.sendChannel(messageDTO, userId);
        } else if (evt instanceof SessionManager.GroupMemberRemoval event) {
            String groupId = event.getGroupId();
            String leavingUserId = event.getMemberId();
            // Unbind group channel
            SessionManager.unbindChannelGroup(groupId, leavingUserId);
            // Update contacts in Redis
            String contactKey = RedisConstants.USER_CONTACT_KEY + leavingUserId;
            redisUtils.removeFromSet(contactKey, groupId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String userId = channel.attr(Attributes.AUTH_USER_ID).get();
        if (!StringUtils.isEmpty(userId)) {
            // Unbind channel
            SessionManager.unBindChannel(userId);
            // Delete Redis heartbeat
            String heartbeatKey = RedisConstants.WS_USER_HEARTBEAT_KEY + userId;
            redisUtils.delete(heartbeatKey);
            // Delete Redis token when forcing offline
            Boolean isForcedOffline = channel.attr(Attributes.FORCE_OFFLINE).get();
            if (isForcedOffline != null && isForcedOffline) {
                String digestKey = RedisConstants.WS_TOKEN_DIGEST_KEY + userId;
                String digest = (String) redisUtils.get(digestKey);
                if (digest != null) {
                    String tokenKey = RedisConstants.WS_TOKEN_KEY + digest;
                    redisUtils.delete(Arrays.asList(digestKey, tokenKey));
                }
            }
            // Update user info
            UserInfo update = new UserInfo();
            update.setUserId(userId);
            update.setLastOfflineTime(new Date());
            this.userInfoMapper.updateUserInfo(update);
        }
        super.channelInactive(ctx);
    }
}
