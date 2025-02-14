package com.xl.rpc.client.queue.mem;

import com.xl.rpc.message.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class MQProvider {
    public static int threadCnt = Runtime.getRuntime().availableProcessors() * 2;
    private static Map<Integer, LinkedBlockingQueue<Message>>
            toRPCMsgQueue = new HashMap<>();

    private static Random random = new Random();

    static {
        for (int i = 0; i < threadCnt; i++) {
            toRPCMsgQueue.put(i, new LinkedBlockingQueue<>());
        }
    }


    /**
     * 随机获得队列
     *
     * @return
     */
    public static LinkedBlockingQueue<Message> getToRPCMsgQueueByRandom() {
        int index = random.nextInt(threadCnt);
        return toRPCMsgQueue.get(index);
    }

    /**
     * 根据索引获得队列
     *
     * @return
     */
    public static LinkedBlockingQueue<Message> getToRPCMsgQueueByIndex(int index) {
        return toRPCMsgQueue.get(index);
    }


    public static int getToRPCMsgQueueSize() {
        return toRPCMsgQueue.size();
    }
}
