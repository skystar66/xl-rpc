package com.xlRpc.kafka.client.statics;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 吞吐量监控
 *
 * @author xuliang
 * @version 2024年10月24日
 */
public class AsyncThroughputMonitor {

    private final AtomicInteger taskCount = new AtomicInteger(0);
    private final int monitoringIntervalSeconds;
    private long startTime;

    public AsyncThroughputMonitor(int monitoringIntervalSeconds) {
        this.monitoringIntervalSeconds = monitoringIntervalSeconds;
        this.startTime = System.currentTimeMillis();
//        startMonitoring();
    }

    // 调用此方法来记录每个已完成的任务
    public void recordTaskCompletion() {
        taskCount.incrementAndGet();
    }

    // 启动异步监控
    public void startMonitoring() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::calculateAndPrintThroughput,
                monitoringIntervalSeconds,
                monitoringIntervalSeconds,
                TimeUnit.SECONDS);
    }

    // 计算并打印当前的吞吐量
    public double calculateAndPrintThroughput() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        int completedTasks = taskCount.getAndSet(0);
        startTime = currentTime;
        double throughput = (double) completedTasks / (elapsedTime / 1000.0);
        return throughput;
    }

    public static void main(String[] args) {
        AsyncThroughputMonitor monitor = new AsyncThroughputMonitor(10); // 每10秒计算一次吞吐量

        // 模拟任务完成
        for (int i = 0; i < 100; i++) {
            monitor.recordTaskCompletion();
            try {
                Thread.sleep(100); // 模拟任务执行时间
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
