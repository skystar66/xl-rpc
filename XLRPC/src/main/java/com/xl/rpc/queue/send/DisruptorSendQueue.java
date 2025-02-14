package com.xl.rpc.queue.send;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import com.xl.rpc.message.Message;
import com.xl.rpc.queue.MessageEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 16:47:06
 */
@Slf4j
public class DisruptorSendQueue {

    private RingBuffer<MessageEvent<Message>> ringBuffer;

    public DisruptorSendQueue() {
        init();

    }

    public void produceData(Message msg) {
        long sequence = ringBuffer.next(); // 获得下一个Event槽的下标
        try {
            // 给Event填充数据
            MessageEvent event = ringBuffer.get(sequence);
            event.setMsg(msg);

        } finally {
            // 发布Event，激活观察者去消费， 将sequence传递给该消费者
            // 注意，最后的 ringBuffer.publish() 方法必须包含在 finally 中以确保必须得到调用；如果某个请求的 sequence 未被提交，将会堵塞后续的发布操作或者其它的 producer。
            ringBuffer.publish(sequence);
        }
//        log.info("生产消息=======");
    }

    public void init() {
        try {
            // 指定 ring buffer字节大小，必需为2的N次方(能将求模运算转为位运算提高效率 )，否则影响性能
            int bufferSize = 1024 * 1024* 2;
            //固定线程数
            ExecutorService executor = Executors.newFixedThreadPool(1);
            EventFactory<MessageEvent<Message>> factory = new EventFactory<MessageEvent<Message>>() {
                @Override
                public MessageEvent newInstance() {
                    return new MessageEvent();
                }
            };
            // 创建ringBuffer
            ringBuffer = RingBuffer.create(ProducerType.SINGLE, factory, bufferSize,
                    new YieldingWaitStrategy());
            SequenceBarrier barriers = ringBuffer.newBarrier();
            DisruptorConsumer[] consumers = new DisruptorConsumer[1];
            for (int i = 0; i < consumers.length; i++) {
                consumers[i] = new DisruptorConsumer();
            }
            WorkerPool<MessageEvent<Message>> workerPool = new WorkerPool<MessageEvent<Message>>(ringBuffer, barriers,
                    new EventExceptionHandler(), consumers);
            ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
            workerPool.start(executor);
        } catch (Exception e) {
            log.error("init queue error:", e);
        }

    }

    @Slf4j
    public static class EventExceptionHandler implements ExceptionHandler {

        @Override
        public void handleEventException(Throwable ex, long sequence, Object event) {
            log.error("handleEventException：" + ex);
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            log.error("handleEventException：" + ex);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            log.error("handleOnStartException：" + ex);
        }

    }


}
