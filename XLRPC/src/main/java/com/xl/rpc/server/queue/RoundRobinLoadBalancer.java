package com.xl.rpc.server.queue;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer {
    private String[] servers;  // 存储服务器列表
    private volatile int index = 0;
    private int threadCnt = 0;
    private int currentIndex = 0;

    // 构造函数，初始化服务器列表
    public RoundRobinLoadBalancer(int threadCount) {
        this.threadCnt = threadCount;
    }

    // 获取下一个服务器
    public Pair<Integer, String> getNextServer() {
        // 如果没有服务器，则返回 null
        if (servers.length == 0) {
            return Pair.of(-1, null);
        }

        return null;
//
//        // 使用位运算代替取余操作，当前索引递增
//        currentIndex = (currentIndex + 1) & (servers.length - 1);  // 位运算实现循环
//
//        // 返回当前服务器
//        return Pair.of(currentIndex, servers[currentIndex]);
    }

    public Integer getNextNum() {
        // 如果没有服务器，则返回 null
        // 使用位运算代替取余操作，当前索引递增

//        int current = currentIndex.incrementAndGet();
////        currentIndex = (currentIndex + 1) & (threadCnt - 1);  // 位运算实现循环
//        int pos = current & threadCnt; // 位运算替代 %，前提是 size 是 2 的幂

//        int current;
//        synchronized (this) {
//            current = index++;
//            if (index == Integer.MAX_VALUE) {
//                index = 0; // 重置防溢出
//            }
//        }

        // 返回当前服务器
        return currentIndex++ % threadCnt;
    }

    // 主函数，测试负载均衡器
    public static void main(String[] args) {
//        // 模拟一组服务器，数量是2的幂次方
//        String[] servers = {"Server1", "Server2", "Server3", "Server4"};
//
//        // 创建一个负载均衡器
        RoundRobinLoadBalancer loadBalancer = new RoundRobinLoadBalancer(32);
//
        int threadNum = 100;
        int cnt = 100000;
//
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);

        CountDownLatch countDownLatch=new CountDownLatch(threadNum*cnt);
        Map<Integer, AtomicInteger> map=new ConcurrentHashMap<>();
        for (int i = 0; i < threadNum; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < cnt; j++) {
                    map.computeIfAbsent(loadBalancer.getNextNum(), k -> new AtomicInteger()).incrementAndGet();
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<Integer, AtomicInteger> entry : map.entrySet()) {
            System.out.println("Server " + entry.getKey() + ": " + entry.getValue().get());
        }

        long startTime = System.currentTimeMillis();
        // 模拟一系列请求，打印每次请求分配到的服务器
        for (int i = 0; i < threadNum * cnt; i++) {
//            loadBalancer.getNextServer();
            loadBalancer.getNextNum();
//            System.out.println("Request " + (i + 1) + " is assigned to: " + loadBalancer.getNextNum());
        }
        System.out.println(System.currentTimeMillis() - startTime);
    }
}

