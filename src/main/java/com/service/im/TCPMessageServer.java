package com.service.im;

import com.service.im.processor.ProcessorManager;
import com.service.im.protobuf.Protobuf;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TCPMessageServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TCPMessageServer.class);
    private static final int READER_IDLE_TIME_SECONDS = 30;
    private static final int PORT = 6969;

    public static void main(String[] args) {
        final ProcessorManager manager = new ProcessorManager();
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup();
        EventLoopGroup workLoopGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossLoopGroup, workLoopGroup);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new IdleStateHandler(READER_IDLE_TIME_SECONDS, 0, 0));
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                pipeline.addLast(new ProtobufDecoder(Protobuf.Body.getDefaultInstance()));
                pipeline.addLast(new MessageHandler(manager));
                pipeline.addLast(new ProtobufEncoder());
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
            }
        });
        try {
            ChannelFuture f = bootstrap.bind(PORT).sync();
            if (f.isSuccess()) {
                manager.start();
                LOGGER.info("服务器成功启动!");
                f.channel().closeFuture().sync();
            } else {
                LOGGER.error("服务器启动失败!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("服务器启动失败!", e);
        } finally {
            workLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
            manager.stop();
        }
    }

}
