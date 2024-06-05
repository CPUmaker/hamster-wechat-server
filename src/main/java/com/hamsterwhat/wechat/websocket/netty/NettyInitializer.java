package com.hamsterwhat.wechat.websocket.netty;

import com.hamsterwhat.wechat.entity.constants.AppProperties;
import com.hamsterwhat.wechat.websocket.netty.handler.AuthHandler;
import com.hamsterwhat.wechat.websocket.netty.handler.WsIdleStateHandler;
import com.hamsterwhat.wechat.websocket.netty.handler.WebSocketFrameHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class NettyInitializer extends ChannelInitializer<SocketChannel> {

    @Resource
    private AuthHandler authHandler;

    @Resource
    private WebSocketFrameHandler webSocketFrameHandler;

    @Resource
    private AppProperties appProperties;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // Add heartbeat
        pipeline.addLast(new WsIdleStateHandler(
                this.appProperties.getWebSocket().getReaderIdleTimeSeconds()
        ));
        // Add support for http protocol
        pipeline.addLast(new HttpServerCodec());
        // Aggregate decoding from httpRequest/httpContent/lastHttpContent to fullHttpRequest
        pipeline.addLast(new HttpObjectAggregator(65536));

        // CHeck Authentication before handshake
        pipeline.addLast(authHandler);
        // Upgrade http to ws
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler(
                appProperties.getWebSocket().getWsPath(), null, true));
        // Business logic
        pipeline.addLast(webSocketFrameHandler);
    }
}
