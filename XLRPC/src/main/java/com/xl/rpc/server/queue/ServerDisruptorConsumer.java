package com.xl.rpc.server.queue;

import com.lmax.disruptor.WorkHandler;
import com.xl.rpc.client.queue.MessageEventClient;
import com.xl.rpc.message.Message;
import com.xl.rpc.message.MessageBuf;
import com.xl.rpc.server.statics.StaticsManager;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 15:55:04
 */
@Slf4j
public class ServerDisruptorConsumer implements WorkHandler<MessageEventClient<Message>> {
//    private ExecutorService executorService = new ThreadPoolExecutor(
//            32, 64, 600, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
//    );

//    private ExecutorService executorService=Executors.newFixedThreadPool(8);

//    AsyncThroughputMonitor throughputMonitor = new AsyncThroughputMonitor(1);


    public static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


//    static {
//
//        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {
//                log.info("线程ID：{},ClientDisruptorConsumer count:{}", Thread.currentThread().getId(), count.get());
//            }
//        }, 1, 5, TimeUnit.SECONDS);
//    }


    @Override
    public void onEvent(MessageEventClient<Message> imMessageMessageEventClient) throws Exception {

        Message msg = imMessageMessageEventClient.getMsg();
        try {

            MessageBuf.IMMessage imMessage = MessageBuf.IMMessage.parseFrom(msg.getContent());
            StaticsManager.getInstance().msgQpsIncrement();
            StaticsManager.getInstance().msgDelayReport((long) msg.getId(),
                    System.currentTimeMillis() - imMessage.getServerTime());
        } finally {
            imMessageMessageEventClient.clear(); //清楚GC
        }

    }

}
