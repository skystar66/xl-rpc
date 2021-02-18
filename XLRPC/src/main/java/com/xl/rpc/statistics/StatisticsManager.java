package com.xl.rpc.statistics;


import com.xl.rpc.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @auth xl
 * @date 2020/12c/25
 * 服务统计 调用量等信息
 */
public class StatisticsManager {


    private static Logger logger = LoggerFactory.getLogger(StatisticsManager.class);

    private static volatile StatisticsManager instance;

    public static StatisticsManager getInstance() {
        if (instance == null) {
            synchronized (StatisticsManager.class) {
                if (instance == null) {
                    instance = new StatisticsManager();
                }
            }
        }
        return instance;
    }

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    public StatisticsManager(){
        printStatisticServer();
    }

    private Map<String, StatisticsInfo> map = new ConcurrentHashMap<>();
    private Map<String, AtomicLong> qpsMap = new ConcurrentHashMap<>();


    public void start(String ipport,String action, Message message) {


    }

    public void success(String ipport,String action, Message message) {
        AtomicLong atomicLong = qpsMap.get(action);
        if (atomicLong == null) {
            atomicLong = new AtomicLong(0);
            qpsMap.putIfAbsent(action,atomicLong);
        }
        atomicLong.incrementAndGet();

    }

    public void fail(String ipport,String action, Throwable t) {

        logger.error("action >>>>>>> error:{}",t
        );
    }


    /**打印服务调用量,上报服务日志调用量 http 每10s or 100s等 上报一次---->>>>*/
    public void printStatisticServer(){
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, AtomicLong> stringAtomicLongEntry : qpsMap.entrySet()) {
                    logger.info("@@@@@@QPS serverName:{} serverQps:{}",stringAtomicLongEntry.getKey(),
                            stringAtomicLongEntry.getValue().get());
                }
            }
        },0,10*1000, TimeUnit.MILLISECONDS);
    }



    public static class StatisticsInfo {

        private String action;

        private String message;

        private String ipPort;

        private AtomicLong atomicLong=new AtomicLong(0);


        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getIpPort() {
            return ipPort;
        }

        public void setIpPort(String ipPort) {
            this.ipPort = ipPort;
        }

        public AtomicLong getAtomicLong() {
            return atomicLong;
        }

        public void setAtomicLong(AtomicLong atomicLong) {
            this.atomicLong = atomicLong;
        }
    }
}
