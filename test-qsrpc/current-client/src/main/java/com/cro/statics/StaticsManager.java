package com.cro.statics;

import com.google.common.util.concurrent.AtomicDouble;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

import java.util.OptionalDouble;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class StaticsManager {

    //消息延时统计
    private static ConcurrentHashMap<Long, Long> msgDelayMap = new ConcurrentHashMap<>();
    //消息QPS
    private static AtomicDouble msgQpsMap = new AtomicDouble(0);
    //成功率
    private static AtomicLong msgSuccessRateMap = new AtomicLong(0);
    //错误率
    private static AtomicLong msgErrorRateMap = new AtomicLong(0);
    //线程数
    //批处理数量
    //1s统计一次
    private AsyncThroughputMonitor asyncThroughputMonitor = new AsyncThroughputMonitor(1);


    private static AtomicLong lastUpdateTs = new AtomicLong(0);

    // 内部静态类方式
    private static class InstanceHolder {
        private static StaticsManager instance = new StaticsManager();
    }

    public static StaticsManager getInstance() {
        return InstanceHolder.instance;
    }


    public StaticsManager() {
        startMonitoring();
    }


    private void startMonitoring() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::statics,
                1,
                1,
                TimeUnit.SECONDS);
    }

    private void statics() {
        double qps = asyncThroughputMonitor.calculateAndPrintThroughput();
        //输出报告
        System.out.println("RPC Performance Test:");
//        System.out.println("Threads: " + ThreadPoolUtils.getKafkaPool().getMaximumPoolSize());
        System.out.println("Average QPS(s): " + qps);
        System.out.println("========================================================================= ");
        System.out.println("========================================================================= ");

    }

    /**
     * 消息QPS
     */
    public void msgQpsIncrement() {
        asyncThroughputMonitor.recordTaskCompletion();
        lastUpdateTs.compareAndSet(0, System.currentTimeMillis());
    }


    /**
     * 消息延时
     */
    public void msgDelayReport(Long msgId, Long delay) {
        msgDelayMap.put(msgId, delay);
    }


    /**
     * 错误率
     */
    public void msgErrorRateReport(Long msgId) {
        msgErrorRateMap.incrementAndGet();
    }

    /**
     * 成功率
     */
    public void msgSuccessRateReport(Long msgId) {
        msgSuccessRateMap.incrementAndGet();
    }
}
