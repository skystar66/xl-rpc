package com.xl.rpc.statistics;


import com.xl.rpc.message.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * @auth xl
 * @date 2020/12c/25
 * 服务统计 调用量等信息
 */
public class StatisticsManager {

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

    private Map<String, StatisticsInfo> map = new HashMap<>();


    public void start(String ipport, Message message) {

    }

    public void success(String ipport, Message message) {

    }

    public void fail(String ipport, Throwable t) {

    }

    public static class StatisticsInfo {

    }
}
