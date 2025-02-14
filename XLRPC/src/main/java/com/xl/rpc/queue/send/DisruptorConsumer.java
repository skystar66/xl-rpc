package com.xl.rpc.queue.send;

import com.lmax.disruptor.WorkHandler;
import com.xl.rpc.message.Message;
import com.xl.rpc.queue.MessageEvent;
import com.xl.rpc.server.manager.ChannelManager;
import com.xl.rpc.utils.AsyncThroughputMonitor;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
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
public class DisruptorConsumer implements WorkHandler<MessageEvent<Message>> {
//    private ExecutorService executorService = new ThreadPoolExecutor(
//            32, 64, 600, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
//    );

//    AsyncThroughputMonitor throughputMonitor = new AsyncThroughputMonitor(1);

    public static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


    public static final AtomicLong count = new AtomicLong(0);

    static {

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("线程ID：{},DisruptorConsumer count:{}",Thread.currentThread().getId(), count.get());
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onEvent(MessageEvent<Message> imMessageMessageEvent) throws Exception {

        // TODO: 2023/10/26 调用router发送IM消息
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
                try {
                    Message message = imMessageMessageEvent.getMsg();
                    /**todo 调用本地代理服务*/
                    Message resMessage = responseMsg(message, "success".getBytes(StandardCharsets.UTF_8));
                    Channel channel = ChannelManager.getRandomChannel();
                    channel.writeAndFlush(resMessage);
//                    throughputMonitor.recordTaskCompletion();
                    count.incrementAndGet();
//                    log.info("消费消息=======");
                } catch (Exception e) {
                    log.error("queue consumer error:{}", e);
                }
//            }
//        });
    }


    /**
     * 封装response
     */
    public Message responseMsg(Message msg, byte[] message) {
        if (msg!=null){
            Message msg_cb = new Message();
            msg_cb.setId(msg.getId());
            msg_cb.setZip(msg.getZip());
            msg_cb.setVer(msg.getVer());
            msg_cb.setContent(message);
            return msg_cb;
        }
        log.info("msg is null.");
        return new Message();

    }

}
