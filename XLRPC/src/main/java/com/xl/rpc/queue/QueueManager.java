package com.xl.rpc.queue;


import com.xl.rpc.message.Message;
import com.xl.rpc.queue.send.DisruptorSendQueue;
import com.xl.rpc.utils.HashCodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 15:11:56
 */
public class QueueManager {

    private static final Logger log = LoggerFactory.getLogger(QueueManager.class);
    private static int outThreadCount = 1;


    private static class InstanceHolder {
        public static final QueueManager instance = new QueueManager();
    }

    public static QueueManager getInstance() {
        return InstanceHolder.instance;
    }

    public static final Map<Integer, DisruptorSendQueue> disruptorSendQueueMap = new HashMap<>();


    public QueueManager() {
//        init();
    }


    static {
        for (int i = 0; i < outThreadCount; i++) {
            disruptorSendQueueMap.put(i,
                    new DisruptorSendQueue());
            System.out.println("初始化队列======"+i);
        }
    }

    /**
     * 得到与index相匹配的队列
     *
     * @param index
     * @return
     */
    public static DisruptorSendQueue getDisruptorSendQueueByIndex(int index) {
        return disruptorSendQueueMap.get(index);
    }


    /**
     * 得到与key 取模的队列
     *
     * @param key
     * @return
     */
    public  static DisruptorSendQueue getDisruptorSendQueueByIndex(String key) {
        int index = HashCodeUtils.getHashCode(key) % outThreadCount;
        return disruptorSendQueueMap.get(index);
    }

    public static void pushOutMessage(Message msg) {
        if (null != msg) {
            getDisruptorSendQueueByIndex(String.valueOf(msg.getId())).produceData(msg);
        }
    }


}
