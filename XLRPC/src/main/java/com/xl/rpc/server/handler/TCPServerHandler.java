package com.xl.rpc.server.handler;

import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.message.Message;
import com.xl.rpc.queue.QueueManager;
import com.xl.rpc.server.queue.ServerQueueManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class TCPServerHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(TCPServerHandler.class);


    private MessageListener messageListener;

    //    private ExecutorService executorService = Executors.newFixedThreadPool(256);
//    private ExecutorService executorService = new ThreadPoolExecutor(
//            32, 64, 600,
//            TimeUnit.SECONDS, new LinkedBlockingQueue<>()
//    );

    public TCPServerHandler(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);


    public static final AtomicLong count = new AtomicLong(0);

    static {

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                logger.info("server Handler count:{}", count.get());
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channel, final Message message) throws Exception {

        ServerQueueManager.getInst().produce(message);
        //todo 暂时注释掉
//        executorService.submit(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    /**调用本地代理服务*/
//                    byte[] result = messageListener.onMessage(message.getId(),
//                            message.getContent());
//                    Message resMessage = responseMsg(message, result);
//                    //默认不接收回调
//                    if (resMessage.getVer() == 2) {
//                        channel.writeAndFlush(resMessage);
//                    }
////                    count.incrementAndGet();
//                } catch (Exception e) {
//                    logger.error("tcp server handler error:{}", e);
//                }
//            }
//        });

//
//            try {
////            logger.info("收到消息");
//                count.incrementAndGet();
//                QueueManager.pushOutMessage(message);
//
//            } catch (Exception e) {
//                logger.error("exception ", e);
//            }


    }

    /**
     * 封装response
     */
    public Message responseMsg(Message msg, byte[] message) {
        Message msg_cb = new Message();
        msg_cb.setId(msg.getId());
        msg_cb.setZip(msg.getZip());
        msg_cb.setVer(msg.getVer());
        msg_cb.setContent(message);
        return msg_cb;
    }

}
