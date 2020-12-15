package com.xl.rpc.client.monitor;


import com.xl.rpc.client.manager.RpcClientManager;
import com.xl.rpc.mq.MQProvider;
import com.xl.rpc.mq.MessageQueue;
import com.xl.rpc.utils.RPCConstants;
import com.xl.rpc.zk.NodeInfo;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * rpc 连接监控
 */
@Slf4j
public class ConnectQueueMonitor {


    private static class InstanceHolder {
        public static final ConnectQueueMonitor instance = new ConnectQueueMonitor();
    }

    public static ConnectQueueMonitor getInstance() {
        return InstanceHolder.instance;
    }


    private ExecutorService msgSenderExecutor;

    public ConnectQueueMonitor() {
    }

    public void start() {

        msgSenderExecutor = Executors.newFixedThreadPool(RPCConstants.retryQueueCount);

        for (int i = 0; i < RPCConstants.retryQueueCount; i++) {
            msgSenderExecutor.execute(new ConnectConsumerWorker(i));
        }
    }


    private class ConnectConsumerWorker implements Runnable {

        private final Duration timeout = Duration.ofMillis(100);
        private final MessageQueue<NodeInfo> retryConnectQueue;

        public ConnectConsumerWorker(int index) {
            this.retryConnectQueue = MQProvider.getRetryConnectQueueByIndex(index);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (null != retryConnectQueue) {
                        NodeInfo msg = retryConnectQueue.pop(timeout);
                        RpcClientManager.getInstance().connect(msg, msg.getRpcServerIndex());
                    }
                } catch (Exception ignore) {
                    log.warn("fromRPCMsgQueue.pop", ignore);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }


}
