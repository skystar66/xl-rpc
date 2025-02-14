package com.xl.rpc.client.queue.mem;


import com.xl.rpc.callback.Callback;
import com.xl.rpc.callback.CallbackPool;
import com.xl.rpc.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.*;

@Slf4j
public class RpcUpMsgConsumer {


    private static class InstanceHolder {
        public static final RpcUpMsgConsumer instance = new RpcUpMsgConsumer();
    }

    public static RpcUpMsgConsumer getInstance() {
        return InstanceHolder.instance;
    }

    public void start() {
        ExecutorService detectThreadPool = Executors.newFixedThreadPool(MQProvider.threadCnt);
        for (int i = 0; i < MQProvider.threadCnt; i++) {
            detectThreadPool.execute(new RpcMsgSender(
                    i % MQProvider.threadCnt));
        }
        log.info("RpcMsgConsumer Async Queue Start！！Thread Count:{}", MQProvider.threadCnt);
    }

    private static volatile boolean stop_flag = false;

    public void stop() {
        log.info("Stopping GroupChatMsgQueueConsumer Queue...");
        stop_flag = true;
    }

    private ExecutorService executor = new ThreadPoolExecutor(
            32, 64, 600, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
    );

    private class RpcMsgSender implements Runnable {


        private int index;
        private final Duration timeout;


        private LinkedBlockingQueue<Message> toRPCMsgQueue = null;

        private RpcMsgSender(int i) {
            index = i;
            timeout = Duration.ofMillis(100);
            toRPCMsgQueue = MQProvider.getToRPCMsgQueueByIndex(index);
        }


        @Override
        public void run() {
            while (true) {
                try {
                    if (stop_flag) {
                        break;
                    }
                    Message msg = toRPCMsgQueue.take();
                    if (msg != null) {
                        executor.submit(() -> {
                            Callback<Message> cb = (Callback<Message>) CallbackPool.remove(msg.getId());
                            if (cb == null) {
                                //找不到回调//可能超时被清理了
                                log.warn("Receive msg from server but no context found, requestId=" + msg.getId() + ",");
                                return;
                            }
                            cb.handleResult(msg);
                        });
                    }
                } catch (Exception ignore) {
                }
            }
        }
    }


}
