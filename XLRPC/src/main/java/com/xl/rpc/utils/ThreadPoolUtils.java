package com.xl.rpc.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolUtils {


    private static final class ThreadPoolUtilsHold{
        private static final ThreadPoolUtils instance = new ThreadPoolUtils();
    }

    public static ThreadPoolUtils getInstance(){
        return ThreadPoolUtilsHold.instance;
    }

    private ThreadPoolUtils(){
        init();
    }

    private static ExecutorService executorService;

    public ExecutorService init(){
        executorService = Executors.newFixedThreadPool(50);
        return executorService;
    }

    public void shutDown(){

        executorService.shutdown();
    }


    public boolean isTerminated(){

        return executorService.isTerminated();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
    public void setExecutorService(ExecutorService executorService) {
       executorService = executorService;
    }
}
