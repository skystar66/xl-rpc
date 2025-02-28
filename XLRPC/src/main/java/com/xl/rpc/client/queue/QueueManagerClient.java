package com.xl.rpc.client.queue;


import com.xl.rpc.client.queue.send.ClientDisruptorSendQueue;
import com.xl.rpc.message.Message;
import com.xl.rpc.utils.HashCodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 15:11:56
 */
public class QueueManagerClient {

    private static final Logger log = LoggerFactory.getLogger(QueueManagerClient.class);
    // TODO: 2023/10/26
    private static int outThreadCount = 1;


    private static class InstanceHolder {
        public static final QueueManagerClient instance = new QueueManagerClient();
    }

    public static QueueManagerClient getInstance() {
        return InstanceHolder.instance;
    }

    public final Map<Integer, ClientDisruptorSendQueue> disruptorSendQueueMap = new HashMap<>();


    public QueueManagerClient() {
        init();
    }

    Random random=new Random();

    public void init() {
        for (int i = 0; i < outThreadCount; i++) {
            disruptorSendQueueMap.put(i,
                    new ClientDisruptorSendQueue());
        }
    }

    /**
     * 得到与index相匹配的队列
     *
     * @param index
     * @return
     */
    public ClientDisruptorSendQueue getDisruptorSendQueueByIndex(int index) {
        return disruptorSendQueueMap.get(index);
    }


    /**
     * 得到与index相匹配的队列
     *
     * @param index
     * @return
     */
    public ClientDisruptorSendQueue getRandomDisruptorSendQueueByIndex() {


        int index = random.nextInt(outThreadCount);

        return disruptorSendQueueMap.get(index-1);
    }


    /**
     * 得到与key 取模的队列
     *
     * @param key
     * @return
     */
    public ClientDisruptorSendQueue getDisruptorSendQueueByIndex(String key) {
        int index = HashCodeUtils.getHashCode(key) % outThreadCount;
        ClientDisruptorSendQueue clientDisruptorSendQueue = disruptorSendQueueMap.get(index-1);
        if (null == clientDisruptorSendQueue) {
            log.error("queue index:" + index);
            return null;
        }
        return clientDisruptorSendQueue;
    }

    public void pushOutMessage(Message msg) {
        if (null != msg) {
            ClientDisruptorSendQueue randomDisruptorSendQueueByIndex =null;
            try {
                 randomDisruptorSendQueueByIndex = getRandomDisruptorSendQueueByIndex();

            }catch (Exception e){
                log.error("获取随机队列err:",e);
            }

            if (randomDisruptorSendQueueByIndex!=null){
                randomDisruptorSendQueueByIndex.produceData(msg);
            }



        }
    }


}
