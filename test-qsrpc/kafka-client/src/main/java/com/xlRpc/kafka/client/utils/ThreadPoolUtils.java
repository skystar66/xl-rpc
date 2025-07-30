package com.xlRpc.kafka.client.utils;

import lombok.Getter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway-master
 * @description
 * @date 2024/1/26 16:58:19
 */
public class ThreadPoolUtils {


    private static class InstanceHolder {
        public static final ThreadPoolUtils instance = new ThreadPoolUtils();
    }

    public static ThreadPoolUtils getInstance() {
        return InstanceHolder.instance;
    }


    @Getter
    private static ThreadPoolExecutor kafkaPool = new ThreadPoolExecutor(1, 2, 10L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    @Getter
    private static ThreadPoolExecutor bussinessPool = new ThreadPoolExecutor(128, 256, 10L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());


}
