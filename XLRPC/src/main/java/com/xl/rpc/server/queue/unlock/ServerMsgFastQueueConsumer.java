package com.xl.rpc.server.queue.unlock;

import com.xl.rpc.message.Message;
import com.xl.rpc.message.MessageBuf;
import com.xl.rpc.server.statics.StaticsManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @date:2021-03-26
 * @desc:
 */
@Slf4j
public class ServerMsgFastQueueConsumer {

    private static Logger logger = LoggerFactory.getLogger(ServerMsgFastQueueConsumer.class);


    private static class InstanceHolder {
        public static final ServerMsgFastQueueConsumer instance = new ServerMsgFastQueueConsumer();
    }

    public static ServerMsgFastQueueConsumer getInstance() {
        return InstanceHolder.instance;
    }

    private static volatile boolean stop_flag = false;

    public void start() {

        ExecutorService detectThreadPool = Executors.newFixedThreadPool(MQProviderS.threadCnt);
        for (int i = 0; i < MQProviderS.threadCnt; i++) {
            detectThreadPool.execute(new Consumer(
                    i % MQProviderS.threadCnt));
        }
        logger.info("GroupChatMsgFastQueueConsumer Queue Start！！Thread Count:{}", MQProviderS.threadCnt);
    }

    public void stop() {
        logger.info("Stopping GroupChatMsgFastQueueConsumer Queue...");
        stop_flag = true;
    }

    public class Consumer implements Runnable {
        private ConcurrentLinkedQueue<Message> groupChatMsgQueue;

        private int index;

        /**
         * @param index 监控对列编号索引
         */
        public Consumer(int index) {
            this.groupChatMsgQueue = MQProviderS.getToRPCMsgQueueByIndex(index);
            this.index = index;
//            print();
        }

        private void processGroupChatMsgQueue() {
            try {
                while (true) {
                    if (stop_flag) {
                        return;
                    }
                    Message poll = groupChatMsgQueue.poll();
                    if (poll != null) {
                        MessageBuf.IMMessage imMessage = MessageBuf.IMMessage.parseFrom(poll.getContent());
                        StaticsManager.getInstance().msgQpsIncrement();
                        StaticsManager.getInstance().msgDelayReport((long) poll.getId(),
                                System.currentTimeMillis() - imMessage.getServerTime());
                    } else {
                        Thread.sleep(100);
                    }
                }
            } catch (Exception ex) {
                log.error("error:{}", ex);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
        }

        public void print(){
            ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                int size = groupChatMsgQueue.size();
                if (size>0){
                    log.info("fast queue index:{},queue.msg.size:{}", index, groupChatMsgQueue.size());
                }
            }, 0, 1, TimeUnit.SECONDS);
        }

        @Override
        public void run() {
            processGroupChatMsgQueue();
        }
    }


}
