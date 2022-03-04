package com.service.im.processor;

import com.service.im.protobuf.Protobuf.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 消息处理器
 */
public class MessageProcessor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProcessor.class);

    /**
     * 处理器ID
     */
    private int id;

    /**
     * 消息处理器名称
     */
    private String name;

    /**
     * 是否运行
     */
    private boolean run = false;

    /**
     * 未处理消息队列
     */
    private BlockingQueue<Body> queue = new LinkedBlockingQueue<Body>();

    public MessageProcessor(int id) {
        this.id = id;
        this.name = String.format("消息处理器 ID=%d", id);
    }

    @Override
    public void run() {
        run = true;
        LOGGER.debug("启动 -> [{}]", name);
        while (run) {
            try {
                Body body = queue.take();
                if (run) {
                    LOGGER.debug("[{}] 执行任务", name);
                    processor(body);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("[{}] 消息处理发生异常! {}", name, e);
            }
        }
        run = false;
        LOGGER.debug("结束 -> [{}]", name);
    }

    private void processor(Body body) {

    }

    public void add(Body body) {
        queue.add(body);
    }

    public int size() {
        return queue.size();
    }

    public boolean isRunning() {
        return run;
    }

    public void stop() {
        run = false;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
