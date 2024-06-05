package com.hamsterwhat.wechat.websocket.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsIdleStateHandler extends IdleStateHandler {

    private static final Logger logger = LoggerFactory.getLogger(WsIdleStateHandler.class);

    public WsIdleStateHandler(Integer readerIdleTimeSeconds) {
        super(readerIdleTimeSeconds, 0, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        String channelId = ctx.channel().id().asLongText();
        logger.debug("Connection timeout, close the channel {}.", channelId);
        ctx.channel().close();
    }
}
