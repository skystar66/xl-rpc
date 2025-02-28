package com.xl.rpc.client.queue.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import com.xl.rpc.client.queue.MessageEventClient;
import com.xl.rpc.client.queue.send.ClientDisruptorConsumer;
import com.xl.rpc.message.Message;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @Date 2020/12/31 16:16
 * @Author wanghao2@rongcloud.cn
 */
public class QueueManager {
    private static QueueManager inst = new QueueManager();
    private static final int BUFFER_SIZE = 1024 * 1024;
    private final RingBuffer<MessageEventClient<Message>> ringBuffer;
//    private final int threadNum = Runtime.getRuntime().availableProcessors() * 2;
        private final int threadNum = Runtime.getRuntime().availableProcessors();
    private final Executor executor = Executors.newFixedThreadPool(this.threadNum);

    private QueueManager() {
        //多生产者
        this.ringBuffer = RingBuffer.create(ProducerType.MULTI, MessageEventClient::new, BUFFER_SIZE,
                new YieldingWaitStrategy() //通过让出大量的CPU资源，调用thread.yield(), 当高负载的情况下，CPU会占用大量资源处理这些挂起的线程，适合于CPU密集型任务
//                new SleepingWaitStrategy() //通过线程休眠的方式，避免CPU高速轮转，适合低延迟的任务
//                new BusySpinWaitStrategy() // 通过自旋的方式，但会占用大量CPU资源，适合低延迟的任务
        );
        SequenceBarrier barriers = this.ringBuffer.newBarrier();
        ClientDisruptorConsumer[] handlers = new ClientDisruptorConsumer[this.threadNum];
        for (int i = 0; i < this.threadNum; i++) {
            ClientDisruptorConsumer handler = new ClientDisruptorConsumer();
            handlers[i] = handler;
        }
        WorkerPool<MessageEventClient<Message>> workerPool
                = new WorkerPool<>(this.ringBuffer, barriers, new EventExceptionHandler(), handlers);
        this.ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(this.executor);
    }

    public static QueueManager getInst() {
        return inst;
    }

    public void produce(Message event) {
        long sequence = this.ringBuffer.next();
        try {
            // 给Event填充数据
            MessageEventClient message = ringBuffer.get(sequence);
            message.setMsg(event);
        } finally {
            this.ringBuffer.publish(sequence);
        }
    }
}
