package com.hamsterwhat.wechat.websocket.netty.handler;

import com.hamsterwhat.wechat.entity.constants.RedisConstants;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.utils.RedisUtils;
import com.hamsterwhat.wechat.websocket.netty.attribute.Attributes;
import com.hamsterwhat.wechat.websocket.netty.utils.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
public class AuthHandler extends ChannelInboundHandlerAdapter {

    @Resource
    RedisUtils redisUtils;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpMessage httpMessage) {
            // Check authentication
            HttpHeaders headers = httpMessage.headers();
            String token = headers.get(HttpHeaderNames.AUTHORIZATION);
            if (token == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_401);
            }

            // Fetch user info from Redis
            String tokenKey = RedisConstants.WS_TOKEN_KEY + token;
            TokenUserInfoDTO tokenUserInfoDTO = (TokenUserInfoDTO) redisUtils.get(tokenKey);
            if (tokenUserInfoDTO == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_401);
            }

            // Add context
            String userId = tokenUserInfoDTO.getUserId();
            ctx.channel().attr(Attributes.AUTH_USER_ID).set(userId);
            SessionManager.bindChannel(userId, ctx.channel());

            ctx.pipeline().remove(this);
        }
        super.channelRead(ctx, msg);
    }
}
