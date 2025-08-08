package com.xl.rpc.server.queue;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WorkerPool;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import com.xl.rpc.client.queue.MessageEventClient;
import com.xl.rpc.client.queue.disruptor.EventExceptionHandler;
import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.message.Message;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @Date 2020/12/31 16:16
 * @Author wanghao2@rongcloud.cn
 */
public class ServerQueueManager {
    private static ServerQueueManager inst = new ServerQueueManager();
    private static final int BUFFER_SIZE = 8*1024 * 1024;
    private final RingBuffer<MessageEventClient<Message>> ringBuffer;
//    private final int threadNum = Runtime.getRuntime().availableProcessors() * 2;
//        private final int threadNum = Runtime.getRuntime().availableProcessors();
        private final int threadNum = 2;
    private final Executor executor = Executors.newFixedThreadPool(this.threadNum);

    private ServerQueueManager() {
        //多生产者
        this.ringBuffer = RingBuffer.create(ProducerType.MULTI, MessageEventClient::new, BUFFER_SIZE,
                new YieldingWaitStrategy() //通过让出大量的CPU资源，调用thread.yield(), 当高负载的情况下，CPU会占用大量资源处理这些挂起的线程，适合于CPU密集型任务
//                new SleepingWaitStrategy() //通过线程休眠的方式，避免CPU高速轮转，适合低延迟的任务
//                new BusySpinWaitStrategy() // 通过自旋的方式，但会占用大量CPU资源，适合低延迟的任务
        );
        SequenceBarrier barriers = this.ringBuffer.newBarrier();
        ServerDisruptorConsumer[] handlers = new ServerDisruptorConsumer[this.threadNum];
        for (int i = 0; i < this.threadNum; i++) {
            ServerDisruptorConsumer handler = new ServerDisruptorConsumer();
            handlers[i] = handler;
        }
        WorkerPool<MessageEventClient<Message>> workerPool
                = new WorkerPool<>(this.ringBuffer, barriers, new EventExceptionHandler(), handlers);
        this.ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(this.executor);
    }

    public static ServerQueueManager getInst() {
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
