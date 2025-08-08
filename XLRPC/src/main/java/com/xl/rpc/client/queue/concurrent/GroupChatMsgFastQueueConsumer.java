package com.xl.rpc.client.queue.concurrent;

import com.xl.rpc.callback.Callback;
import com.xl.rpc.callback.CallbackPool;
import com.xl.rpc.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * @date:2021-03-26
 * @desc:
 */
@Slf4j
public class GroupChatMsgFastQueueConsumer {

    private static Logger logger = LoggerFactory.getLogger(GroupChatMsgFastQueueConsumer.class);


    private static class InstanceHolder {
        public static final GroupChatMsgFastQueueConsumer instance = new GroupChatMsgFastQueueConsumer();
    }

    public static GroupChatMsgFastQueueConsumer getInstance() {
        return InstanceHolder.instance;
    }

    private static volatile boolean stop_flag = false;

    public void start() {

        ExecutorService detectThreadPool = Executors.newFixedThreadPool(MQProviderC.threadCnt);
        for (int i = 0; i < MQProviderC.threadCnt; i++) {
            detectThreadPool.execute(new Consumer(
                    i % MQProviderC.threadCnt));
        }
        logger.info("GroupChatMsgFastQueueConsumer Queue Start！！Thread Count:{}", MQProviderC.threadCnt);
    }

    public void stop() {
        logger.info("Stopping GroupChatMsgFastQueueConsumer Queue...");
        stop_flag = true;
    }

    public class Consumer implements Runnable {
        private ConcurrentLinkedQueue<Message> groupChatMsgQueue;

        private int index;


        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

        /**
         * @param index 监控对列编号索引
         */
        public Consumer(int index) {
            this.groupChatMsgQueue = MQProviderC.getToRPCMsgQueueByIndex(index);
            this.index = index;

//            print();
        }

        private void processGroupChatMsgQueue() {
            try {
                while (true) {
                    if (stop_flag) {
                        return;
                    }
                    List<Message> messageList = dumpGroupMsgQueue();
                    if (!CollectionUtils.isEmpty(messageList)) {
                        for (Message msg : messageList) {
                            if (msg != null) {
//                        executor.submit(() -> {
                                Callback<Message> cb = (Callback<Message>) CallbackPool.remove(msg.getId());
                                if (cb == null) {
                                    //找不到回调//可能超时被清理了
                                    log.warn("Receive msg from server but no context found, requestId=" + msg.getId() + ",");
                                    return;
                                }
                                cb.handleResult(msg);
//                        });
                            }
                        }
                    }
                    Thread.sleep(50);
                }
            } catch (Exception ex) {
                log.error("error:{}", ex);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
        }

        private List<Message> dumpGroupMsgQueue() {
            Message poll = null;
            List<Message> tempArray = new ArrayList<>();
            int take = 0;
            while ((poll = groupChatMsgQueue.poll()) != null) {
                tempArray.add(poll);
                take++;
                if (take >= 10000) {
                    break;
                }
            }
            return tempArray;
        }

        private void print() {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                log.info("fast group queue index:{},queue.msg.size:{}", index, groupChatMsgQueue.size());
            }, 0, 1, TimeUnit.SECONDS);
        }

        @Override
        public void run() {
            processGroupChatMsgQueue();
        }
    }


}
