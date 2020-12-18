package com.xl.rpc.server.handler;

import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServerHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(TCPServerHandler.class);


    private MessageListener messageListener;

    private ExecutorService executorService = Executors.newFixedThreadPool(100);

    public TCPServerHandler(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channel, final Message message) throws Exception {

        executorService.submit(new Runnable() {
            @Override
            public void run() {

                /**调用本地代理服务*/
                byte[] result = messageListener.onMessage(message.getContent());
                Message resMessage = responseMsg(message, result);
                channel.writeAndFlush(resMessage);
            }
        });

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
