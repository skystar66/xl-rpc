package com.xl.rpc.client.queue.send;

import com.lmax.disruptor.WorkHandler;
import com.xl.rpc.callback.Callback;
import com.xl.rpc.callback.CallbackPool;
import com.xl.rpc.client.queue.MessageEventClient;
import com.xl.rpc.message.Message;
import com.xl.rpc.utils.AsyncThroughputMonitor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 15:55:04
 */
@Slf4j
public class ClientDisruptorConsumer implements WorkHandler<MessageEventClient<Message>> {
//    private ExecutorService executorService = new ThreadPoolExecutor(
//            32, 64, 600, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
//    );

//    private ExecutorService executorService=Executors.newFixedThreadPool(8);

//    AsyncThroughputMonitor throughputMonitor = new AsyncThroughputMonitor(1);


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
    public void onEvent(MessageEventClient<Message> imMessageMessageEventClient) throws Exception {
        Message msg = imMessageMessageEventClient.getMsg();
        // TODO: 2023/10/26 调用router发送IM消息
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
                Callback<Message> cb = (Callback<Message>) CallbackPool.remove(msg.getId());
                if (cb == null) {
                    //找不到回调//可能超时被清理了
                    log.warn("Receive msg from server but no context found, requestId=" + msg.getId() + ",");
                    return;
                }
                cb.handleResult(msg);
                count.incrementAndGet();
//            }
//        });
    }

}
