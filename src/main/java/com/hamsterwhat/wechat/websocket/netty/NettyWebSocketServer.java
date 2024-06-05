package com.hamsterwhat.wechat.websocket.netty;

import com.hamsterwhat.wechat.entity.constants.AppProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class NettyWebSocketServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketServer.class);

    @Resource
    private NettyInitializer nettyInitializer;

    @Resource
    private AppProperties appProperties;

    EventLoopGroup bossGroup;

    EventLoopGroup workerGroup;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            // bossGroup establishes tcp connections, workGroup processes read/write duty
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(nettyInitializer);

            try {
                ChannelFuture channelFuture = serverBootstrap
                        .bind(appProperties.getWebSocket().getPort())
                        .syncUninterruptibly();
                logger.info("Netty server started on {}", channelFuture.channel().localAddress());

                channelFuture.channel().closeFuture().syncUninterruptibly();
            } catch (Exception e) {
                logger.error("Failed to boot Netty server", e);
            }
        }).start();
    }

    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
