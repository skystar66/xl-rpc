package com.xl.rpc.callback;


import com.google.common.util.concurrent.Striped;
import com.xl.rpc.exception.RPCException;
import com.xl.rpc.message.Message;

import java.util.concurrent.*;

/**
 * 客户端回调池，用于保存调用发送请求出去的上下文，用于nio异步通信收到服务端响应后回调成功或者失败
 */
public class CallbackPool {

    /**
     * Map默认键数量
     */
    private static final int INITIAL_CAPACITY = 128 * 4 / 3;

    /**
     * Map的扩容装载因子
     */
    private static final float LOAD_FACTOR = 0.75f;

    /**
     * Map的并发度，也就是segament数量，读不锁写锁，
     */
    private static final int CONCURRENCY_LEVEL = Math.max(Runtime.getRuntime().availableProcessors() * 2, 8);

    /**
     * 保存键为调用的唯一标示requestId</tt>
     */
    private static ConcurrentHashMap<Object, Callback<?>> CALLBACK_MAP = new ConcurrentHashMap<>(
            INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);

    private static ConcurrentHashMap<Object, ScheduledFuture<?>> TIMEOUT_MAP = new ConcurrentHashMap<>(INITIAL_CAPACITY,
            LOAD_FACTOR, CONCURRENCY_LEVEL);


    static ConcurrentHashMap<Integer, ConcurrentHashMap<Object, Callback<?>>>
            callbackShardMap = new ConcurrentHashMap<>();


    static ConcurrentHashMap<Integer, ConcurrentHashMap<Object, ScheduledFuture<?>>>
            timeoutShardMap = new ConcurrentHashMap<>();

    private static final int groupCacheShardNums = 16;
    static {
        /**初始化分片*/
        for (int i = 0; i < groupCacheShardNums; i++) {
            ConcurrentHashMap<Object, Callback<?>> CALLBACK_MAP = new ConcurrentHashMap<>(
                    INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
            callbackShardMap.put(i, CALLBACK_MAP);
            ConcurrentHashMap<Object, ScheduledFuture<?>> TIMEOUT_MAP = new ConcurrentHashMap<>(INITIAL_CAPACITY,
                    LOAD_FACTOR, CONCURRENCY_LEVEL);
            timeoutShardMap.put(i, TIMEOUT_MAP);
        }
    }

    public static ConcurrentHashMap<Object, Callback<?>> getCallbackMap(Integer requestId){
        return callbackShardMap.get(getShardIndex(requestId));
    }

    public static ConcurrentHashMap<Object, ScheduledFuture<?>> getTimeoutMap(Integer requestId){
        return timeoutShardMap.get(getShardIndex(requestId));
    }

    public static int getShardIndex(Integer requestId) {
        int shardNum = Math.abs(requestId.hashCode()) % groupCacheShardNums;
        return shardNum;
    }
    /**
     * 放入回调上下文
     *
     * @param requestId requestId
     * @param callback  客户端句柄callback
     * @param timeout   客户端调用超时
     */
    public static void put(final Integer requestId, Callback<?> callback, final int timeout) {
//        CALLBACK_MAP.putIfAbsent(requestId, callback);

        getCallbackMap(requestId).putIfAbsent(requestId, callback);

        if (timeout > 0) {
            ConcurrentHashMap<Object, ScheduledFuture<?>> timeoutMap = getTimeoutMap(requestId);


            timeoutMap.putIfAbsent(requestId, SCHEDULED_EXECUTOR_SERVICE.schedule(new Runnable() {
                @Override
                public void run() {
                    timeoutMap.remove(requestId);
                    @SuppressWarnings("unchecked")
                    Callback<Message> cb = (Callback<Message>) CALLBACK_MAP.remove(requestId);
                    if (cb != null) {
                        cb.handleError(new RPCException("CallbackPool time out: " + timeout + "ms, id:" + requestId));
                    }
                }
            }, timeout, TimeUnit.MILLISECONDS));
        }
    }

    /**
     * 不限时
     */
    public static void put(Integer requestId, Callback<?> callback) {
        put(requestId, callback, 0);
    }


    /**
     * 移除回调上下文
     *
     * @param requestId
     */
    public static Callback<?> remove(Integer requestId) {
//        ScheduledFuture<?> scheduledFuture = TIMEOUT_MAP.remove(requestId);

        ScheduledFuture<?> scheduledFuture =    getTimeoutMap(requestId).remove(requestId);

        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        ConcurrentHashMap<Object, Callback<?>> callbackMap = getCallbackMap(requestId);

        return callbackMap.remove(requestId);
    }

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(30);//处理超时回调,30个线程

}
