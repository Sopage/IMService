package com.service.im.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

public class Redis {
    private static final Logger LOGGER = LoggerFactory.getLogger(Redis.class);
    private static RedisAsyncCommands<String, String> redis;

    private Redis() {

    }

    public static void doInit() {
        try {
            StatefulRedisConnection<String, String> connection = RedisClient.create("redis://password@127.0.0.1:6379/0").connect();
            if (connection.isOpen()) {
                redis = connection.async();
                LOGGER.info("redis 链接成功!!");
            } else {
                LOGGER.error("redis 链接失败!!");
            }
        } catch (Exception e) {
            LOGGER.error("redis 链接失败!!", e);
        }

    }

    public static void putMessage(long uid, String body) {
        redis.rpush(String.valueOf(uid), body);
    }

    public static List<String> getMessage(long uid) {
        try {
            return redis.lrange(String.valueOf(uid), 0, -1).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getLongToBytes(long l) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(l);
        buffer.flip();
        return buffer.array();
    }
}
