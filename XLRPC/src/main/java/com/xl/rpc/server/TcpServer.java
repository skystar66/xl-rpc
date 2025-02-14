package com.xl.rpc.server;

import com.xl.rpc.client.handler.KeepaliveHandler;
import com.xl.rpc.context.NettyContext;
import com.xl.rpc.enums.NettyType;
import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.log.Log;
import com.xl.rpc.protocol.MessageDecoder;
import com.xl.rpc.protocol.MessageEncoder;
import com.xl.rpc.queue.QueueManager;
import com.xl.rpc.reciver.ReciveDataHandler;
import com.xl.rpc.server.handler.TCPServerHandler;
import com.xl.rpc.zk.NodeInfo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class TcpServer {

    private final int PORT;
    private final String IP;
    private NodeInfo nodeInfo;
    private EventLoopGroup workerGroup;
    private EventLoopGroup bossGroup;
    private Channel channel;
    private MessageListener messageListener;


    public TcpServer(NodeInfo nodeInfo, MessageListener messageListener) {
        this.nodeInfo = nodeInfo;
        this.messageListener = messageListener;
        PORT = nodeInfo.getPort();
        IP=nodeInfo.getIp();
    }

    /**
     * 校验连接是否开启
     */
    public boolean isConnect() {
        return (channel != null && channel.isOpen()
                && channel.isActive());
    }
    public boolean start() {
        NettyContext.nettyType= NettyType.server;
        if (isConnect()) return true;

        // Configure SSL.
        final SslContext sslCtx = null;// getSslContext();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(nodeInfo.getCoreThread());// 默认cpu线程*2
        try {
            ServerBootstrap b = new ServerBootstrap();
            // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50
            b.option(ChannelOption.SO_BACKLOG, 1024);
            // 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
            b.option(ChannelOption.SO_KEEPALIVE, true);
            // 用于启用或关闭Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
            b.option(ChannelOption.TCP_NODELAY, false);
            // 缓冲区大小
            b.option(ChannelOption.SO_RCVBUF, 256 * 1024);
            b.option(ChannelOption.SO_SNDBUF, 256 * 1024);

            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    // .handler(new LoggingHandler(LogLevel.INFO)) //日记
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            if (sslCtx != null) {
                                pipeline.addLast(sslCtx.newHandler(ch.alloc()));
                            }

                            pipeline.addLast(new IdleStateHandler(10000,
                                    10000, 10000, TimeUnit.MILLISECONDS));
                            //pipeline.addLast(new LengthFieldBasedFrameDecoder(1024*1024, 0, 4, 0, 0));//组合消息包,参数0是消息最大长度,1,2参数是长度字段的位置,3是长度调整量,4去掉包头
                            pipeline.addLast(new MessageEncoder());// tcp消息编码
                            pipeline.addLast(new MessageDecoder());//  tcp消息解码
                            pipeline.addLast(new KeepaliveHandler());//心跳
                            pipeline.addLast(new ReciveDataHandler());
                            pipeline.addLast(new TCPServerHandler(messageListener));
                        }
                    });

            channel = b.bind(IP,PORT).sync().channel();//tcp监听完成
            log.info("Liveme.Rpc NodeServer Launcher Success! ^_^ IP:{} | PORT:{}",IP,PORT);

//            QueueManager.getInstance();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            close();
            Log.e("Liveme.Rpc NodeServer Launcher Fail T^T: " + e.getMessage());
            return false;
        }finally {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    closeFuture();
                }
            });

        }
    }


    private void closeFuture() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    channel.closeFuture().sync();
                    close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void close() {
        if (isConnect()) channel.close();
        if (bossGroup != null) bossGroup.shutdownGracefully();
        if (workerGroup != null) workerGroup.shutdownGracefully();
        channel = null;
        bossGroup = null;
        workerGroup = null;
        log.info("Liveme.Rpc NodeServer Close Success! -^- IP:{}: | PORT:{}" ,IP,PORT);
    }


}
