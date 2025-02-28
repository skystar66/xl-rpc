package com.xl.rpc.client.queue.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import com.xl.rpc.client.queue.MessageEventClient;
import com.xl.rpc.client.queue.send.ClientDisruptorConsumer;
import com.xl.rpc.message.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @Date 2020/12/31 16:16
 * @Author wanghao2@rongcloud.cn
 */
@Slf4j
public class QueueManager2 {
    private static QueueManager2 inst = new QueueManager2();
    private static final int BUFFER_SIZE = 1024 * 1024;
    private RingBuffer<MessageEventClient<Message>> ringBuffer;
    private final int parallelism = Runtime.getRuntime().availableProcessors();

    private QueueManager2() {
        build();
    }

    public void build() {
        final Disruptor<MessageEventClient<Message>> disruptor = new Disruptor<>(
                new RpcEventFactory(),
                BUFFER_SIZE,
                DaemonThreadFactory.INSTANCE,  // 使用自定义线程工厂
                ProducerType.MULTI,           // 多生产者模式
                new YieldingWaitStrategy()    // 平衡吞吐和延迟
        );

        RpcEventHandler[] handlers = new RpcEventHandler[this.parallelism];
        for (int i = 0; i < this.parallelism; i++) {
            RpcEventHandler handler = new RpcEventHandler();
            handlers[i] = handler;
        }


        // 配置消费者（根据CPU核数设置并行度）
        disruptor.handleEventsWithWorkerPool(handlers);

        // 注册异常处理
        disruptor.setDefaultExceptionHandler(new RpcExceptionHandler());
        disruptor.start();

        this.ringBuffer = disruptor.getRingBuffer();


    }

    public static QueueManager2 getInst() {
        return inst;
    }

    public void produce(Message event) {
        long sequence = this.ringBuffer.next();
        try {
            // 给Event填充数据
            MessageEventClient message = ringBuffer.get(sequence);
            message.setMsg(event);
        }catch (Exception e){
            log.error("produce error",e);
        }finally {
            this.ringBuffer.publish(sequence);
        }
    }


    // 2. 事件工厂
    public class RpcEventFactory implements EventFactory<MessageEventClient<Message>> {
        @Override
        public MessageEventClient<Message> newInstance() {
            return new MessageEventClient<Message>();
        }
    }

    // 7. 异常处理（关键）
    public class RpcExceptionHandler implements ExceptionHandler<MessageEventClient<Message>> {


        @Override
        public void handleOnStartException(Throwable throwable) {
            log.error("handleOnStartException=======");

        }

        @Override
        public void handleOnShutdownException(Throwable throwable) {
            log.error("handleOnShutdownException=======");

        }

        @Override
        public void handleEventException(Throwable ex, long sequence, MessageEventClient<Message> event) {
            // 记录异常并释放资源
//            event.getCtx().close();
            log.error("Process event failed,",ex);

//            log.error("Process event failed", ex);
        }
    }

}
