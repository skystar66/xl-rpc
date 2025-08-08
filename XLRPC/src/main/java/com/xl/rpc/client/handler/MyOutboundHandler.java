package com.xl.rpc.client.handler;

import com.xl.rpc.message.Message;
import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MyOutboundHandler extends ChannelOutboundHandlerAdapter {

    public static AtomicLong totalBytesSent = new AtomicLong();


    private static AtomicLong sendQps = new AtomicLong();

    static {

        ScheduledExecutorService scheduledExecutorService
                = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                long andSet = totalBytesSent.getAndSet(0);

                long MB = andSet / 1024 / 1024;
                if (MB > 0) {
                    log.info("client Handler sendQps:{}/s send bytes:{}MB/s",
                            sendQps.getAndSet(0),
                            MB);
                } else {

                    long KB = andSet / 1024;

                    if (KB > 0) {
                        log.info("client Handler sendQps:{}/s send bytes:{}KB/s",
                                sendQps.getAndSet(0),
                                KB);
                    } else {
                        log.info("client Handler sendQps:{}/s send bytes:{}B/s",
                                sendQps.getAndSet(0),
                                andSet);
                    }


                }
            }
        }, 1, 1, TimeUnit.SECONDS);

//        ServerMsgFastQueueConsumer.getInstance().start();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {





        Message message = (Message) msg;
        totalBytesSent.addAndGet(message.bodyLength());
        sendQps.incrementAndGet();

        super.write(ctx, msg, promise);
    }


//    public boolean backPressureCondition(ChannelHandlerContext ctx) {
//        // 获取当前的发送缓冲区大小
//        long sendBufferSize = ctx.channel().config().getSendBufferSize();
//        long sendBufferUsage = channel.outboundBuffer().size();
//
//        // 如果缓冲区的使用率超过 80%，施加背压
//        return sendBufferUsage > sendBufferSize * 0.8;
//    }

}

