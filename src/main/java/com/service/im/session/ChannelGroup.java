package com.service.im.session;

import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelGroup {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelGroup.class);

    /**
     * TODO 这个
     */
    public static final io.netty.channel.group.ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 存放已登录的Channel
     * key是登录用户的编号UID
     */
    private static final Map<Object, Channel> ONLINE_CHANNEL = new ConcurrentHashMap<>();

    /**
     * 存放连接但未登录的Channel
     */
    private static final Vector<Channel> CONNECTED_CHANNEL = new Vector<>();

    public static synchronized void online(Object key, Channel channel) {
        CONNECTED_CHANNEL.remove(channel);
        Channel c = ONLINE_CHANNEL.get(key);
        if (c != null) {
            c.close();
        }
        ONLINE_CHANNEL.put(key, channel);
    }

    public static Channel getOnlineChannel(Object key) {
        return ONLINE_CHANNEL.get(key);
    }

    public static void connected(Channel channel) {
        CONNECTED_CHANNEL.add(channel);
    }

    public static void disconnect(Channel channel) {
        disconnect(null, channel);
    }

    public static void disconnect(Object key, Channel channel) {
        if (key != null) {
            ONLINE_CHANNEL.remove(key);
        }
        if (channel != null) {
            CONNECTED_CHANNEL.remove(channel);
        }
    }

    public static int getOnlineSize() {
        return ONLINE_CHANNEL.size();
    }

    public static int getConnectedSize() {
        return CONNECTED_CHANNEL.size();
    }

    public synchronized static void checkConnectedTimeOutChannel(long timeOut) {
        Iterator<Channel> iterator = CONNECTED_CHANNEL.iterator();
        long currentTime = System.currentTimeMillis();
        while (iterator.hasNext()) {
            Channel channel = iterator.next();
            Attribute<Session> attribute = channel.attr(Session.KEY);
            Session session = attribute.get();
            if (session == null) {
                LOGGER.warn("{} 没有绑定Session", channel.remoteAddress().toString());
                channel.close();
                iterator.remove();
                continue;
            }
            if ((currentTime - session.connectTime) >= timeOut) {
                LOGGER.warn("{} 长时间没有登录, 关闭连接! {}", channel.remoteAddress(), session.uid);
                channel.close();
                iterator.remove();
            }
        }
    }

}
