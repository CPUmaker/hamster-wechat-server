package com.hamsterwhat.wechat.websocket.netty;

import com.hamsterwhat.wechat.entity.constants.AppProperties;
import com.hamsterwhat.wechat.websocket.netty.handler.HeartbeatHandler;
import com.hamsterwhat.wechat.websocket.netty.handler.WebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class NettyInitializer extends ChannelInitializer<SocketChannel> {

    @Resource
    WebSocketFrameHandler webSocketFrameHandler;

    @Resource
    HeartbeatHandler heartbeatHandler;

    @Resource
    AppProperties appProperties;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // Add support for http protocol
        pipeline.addLast(new HttpServerCodec());
        // Aggregate decoding from httpRequest/httpContent/lastHttpContent to fullHttpRequest
        pipeline.addLast(new HttpObjectAggregator(65536));
        // Add heartbeat
        pipeline.addLast(new IdleStateHandler(
                60, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(heartbeatHandler);
        // Upgrade http to ws
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(
                appProperties.getWebSocket().getWsPath(), null, true));
        // Business logic
        pipeline.addLast(webSocketFrameHandler);
    }
}
