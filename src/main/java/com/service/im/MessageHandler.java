package com.service.im;

import com.service.im.processor.MessageProcessor;
import com.service.im.processor.ProcessorManager;
import com.service.im.protobuf.Protobuf;
import com.service.im.session.ChannelGroup;
import com.service.im.session.Session;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);
    private final ProcessorManager manager;

    public MessageHandler(ProcessorManager manager) {
        this.manager = manager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Protobuf.Body body) {
            Session session = ctx.channel().attr(Session.KEY).get();
            MessageProcessor processor = manager.getMessageProcessor(session);
            if (processor != null) {
                LOGGER.debug("分配消息给 [{}]", processor.getName());
                processor.add(body);
            } else {
                LOGGER.error("无法给 {} 分配消息处理器，消息丢失！", ctx.channel().remoteAddress().toString());
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                LOGGER.warn("{} 连接超时! 服务器关闭此连接!", channel.remoteAddress());
                channel.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        LOGGER.info("有新连接注册:{} -> 当前在线人数{}个, 未登录连接数{}个", channel.remoteAddress().toString(), ChannelGroup.getOnlineSize(), ChannelGroup.getConnectedSize());
        Attribute<Session> attribute = channel.attr(Session.KEY);
        if (attribute.get() == null) {
            LOGGER.info("创建Session -> {}", channel.remoteAddress().toString());
            attribute.set(new Session(System.currentTimeMillis()));
        }
        ChannelGroup.connected(channel);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        LOGGER.info("连接取消注册:{} -> 当前在线人数{}个, 未登录连接数{}个", channel.remoteAddress(), ChannelGroup.getOnlineSize(), ChannelGroup.getConnectedSize());
        Attribute<Session> attribute = channel.attr(Session.KEY);
        Session session = attribute.get();
        if (session != null) {
            ChannelGroup.disconnect(session.uid, channel);
            attribute.remove();
            attribute.set(null);
        } else {
            ChannelGroup.disconnect(channel);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("连接准备就绪:{} -> 当前在线人数{}个, 未登录连接数{}个", ctx.channel().remoteAddress(), ChannelGroup.getOnlineSize(), ChannelGroup.getConnectedSize());

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("连接被关闭:{} -> 当前在线人数{}个, 未登录连接数{}个", ctx.channel().remoteAddress(), ChannelGroup.getOnlineSize(), ChannelGroup.getConnectedSize());
    }

}
