package com.xl.rpc.client.queue.disruptor;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import com.xl.rpc.callback.Callback;
import com.xl.rpc.callback.CallbackPool;
import com.xl.rpc.client.queue.MessageEventClient;
import com.xl.rpc.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RpcEventHandler implements EventHandler<MessageEventClient<Message>>, WorkHandler<MessageEventClient<Message>> {

    public static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


    public static final AtomicLong count = new AtomicLong(0);

    static {

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("线程ID：{},ClientDisruptorConsumer count:{}", Thread.currentThread().getId(), count.get());
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onEvent(MessageEventClient<Message> event) throws Exception {
        try {
            Message msg = event.getMsg();
            // 业务处理逻辑
            Callback<Message> cb = (Callback<Message>) CallbackPool.remove(msg.getId());
            if (cb == null) {
                //找不到回调//可能超时被清理了
                log.warn("Receive msg from server but no context found, requestId=" + msg.getId() + ",");
                return;
            }
            cb.handleResult(msg);
//                log.info("Receive msg from server, requestId=" + msg.getId() + ",");
            count.incrementAndGet();
            // 响应回传示例
        } finally {
            event.clear(); // 重要！清理对象引用
        }
    }

    @Override
    public void onEvent(MessageEventClient<Message> event, long sequence, boolean endOfBatch) {
        try {
            Message msg = event.getMsg();
            // 业务处理逻辑
            Callback<Message> cb = (Callback<Message>) CallbackPool.remove(msg.getId());
            if (cb == null) {
                //找不到回调//可能超时被清理了
                log.warn("Receive msg from server but no context found, requestId=" + msg.getId() + ",");
                return;
            }
            cb.handleResult(msg);
//                log.info("Receive msg from server, requestId=" + msg.getId() + ",");
            count.incrementAndGet();
            // 响应回传示例
        } finally {
            event.clear(); // 重要！清理对象引用
        }
    }
}
