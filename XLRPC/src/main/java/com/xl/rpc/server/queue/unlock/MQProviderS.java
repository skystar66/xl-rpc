package com.xl.rpc.server.queue.unlock;

import com.xl.rpc.message.Message;
import com.xl.rpc.server.queue.RoundRobinLoadBalancer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class MQProviderS {
    public static int threadCnt = Runtime.getRuntime().availableProcessors() * 2;
//        public static int threadCnt =12;
    private static Map<Integer, ConcurrentLinkedQueue<Message>>
            toRPCMsgQueue = new HashMap<>();
    private static ConcurrentLinkedQueue<Message>[] toRPCMsgQueueArray;
    private static Random random = new Random();

    static RoundRobinLoadBalancer roundRobinLoadBalancer = new RoundRobinLoadBalancer(threadCnt);

    static {
        // 初始化数组
        toRPCMsgQueueArray = new ConcurrentLinkedQueue[threadCnt];
        for (int i = 0; i < threadCnt; i++) {
            toRPCMsgQueueArray[i] = new ConcurrentLinkedQueue<>();
        }
    }


    /**
     * 随机获得队列
     *
     * @return
     */
    public static ConcurrentLinkedQueue<Message> getToRPCMsgQueueByRandom() {
//        int index = random.nextInt(threadCnt);

//        int index = ThreadLocalRandom.current().nextInt(threadCnt);
        return toRPCMsgQueueArray[roundRobinLoadBalancer.getNextNum()];
    }

    public static void produce(Message event) {
        getToRPCMsgQueueByRandom().offer(event);
    }

    /**
     * 根据索引获得队列
     *
     * @return
     */
    public static ConcurrentLinkedQueue<Message> getToRPCMsgQueueByIndex(int index) {
        return toRPCMsgQueueArray[index];
    }


    public static int getToRPCMsgQueueSize() {
        return toRPCMsgQueue.size();
    }


    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        int currentIndex = 0;
        int n = 3;
//         AtomicInteger currentIndex = new AtomicInteger(0);  // 使用 AtomicInteger 来确保线程安全

        for (int i = 0; i < 10; i++) {
//            random.nextInt(n);
//            currentIndex = (currentIndex + 1) & (n-1);
            // 使用轮询算法：每次请求返回一个服务器，当前索引增加
//            currentIndex = (currentIndex + 1) % n;
//            int index = currentIndex.getAndIncrement() & (n - 1);  // 使用原子递增并取余
            int index = (currentIndex + 1) & (n - 1);  // 位运算实现循环
            System.out.println(index);
            currentIndex++;
        }
        System.out.println(System.currentTimeMillis() - start);
    }
}
