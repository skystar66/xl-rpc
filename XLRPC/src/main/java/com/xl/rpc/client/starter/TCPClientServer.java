package com.xl.rpc.client.starter;

import com.xl.rpc.client.handler.MyOutboundHandler;
import com.xl.rpc.client.handler.TCPClientHandler;
import com.xl.rpc.client.handler.KeepaliveHandler;
import com.xl.rpc.context.NettyContext;
import com.xl.rpc.enums.NettyType;
import com.xl.rpc.protocol.MessageDecoder;
import com.xl.rpc.protocol.MessageEncoder;
import com.xl.rpc.reciver.ReciveDataHandler;
import com.xl.rpc.zip.Zip;
import com.xl.rpc.zk.NodeInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;

public class TCPClientServer {


    // 内部静态类方式
    private static class InstanceHolder {
        private static TCPClientServer instance = new TCPClientServer();
    }

    public static TCPClientServer getInstance() {
        return TCPClientServer.InstanceHolder.instance;
    }


    // 连接配置,需要再独立成配置类
    private static final int connTimeout = 10 * 1000;

    private static final boolean soKeepalive = true;

    private static final boolean soReuseaddr = true;

    private static final boolean tcpNodelay = true;

    private static final int soRcvbuf = 1024 * 256;

    private static final int soSndbuf = 32 * 1024 * 1024;

    private byte zip, ver;//请求节点的配置

    private SslContext sslContext;

    private Channel channel;
    // TODO 考虑改成静态,所有连接公用同一个线程池
    private static EventLoopGroup bossGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2);


    public TCPClientServer() {
    }

    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    public ChannelFuture connect(NodeInfo nodeInfo) {

        NettyContext.nettyType = NettyType.client;
        if (nodeInfo != null) {
            this.zip = Zip.getInt(nodeInfo.getZip());
            this.ver = nodeInfo.getVer();
        }
        try {
//            bossGroup = new NioEventLoopGroup(zip == 0 ? 1 : 1);//有压缩增加线程数...待定
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connTimeout);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, soKeepalive);
            bootstrap.option(ChannelOption.SO_REUSEADDR, soReuseaddr);
            bootstrap.option(ChannelOption.TCP_NODELAY, tcpNodelay);
            bootstrap.option(ChannelOption.SO_RCVBUF, soRcvbuf);
            bootstrap.option(ChannelOption.SO_SNDBUF, soSndbuf);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            bootstrap.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8*1024 * 1024);  // 32 KB
            bootstrap.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 16*1024 * 1024); // 64 KB

            bootstrap.group(bossGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {

                    ChannelPipeline pipeline = ch.pipeline();
                    if (sslContext != null) {
                        /**处理ssl 认证 ，有需求的可以添加上*/
                        pipeline.addLast(sslContext.newHandler(ch.alloc()));
                    }

                    pipeline.addLast(MessageEncoder.INSTANCE);// tcp消息编码
                    pipeline.addLast(new MessageDecoder());// tcp消息解码

                    pipeline.addLast(new KeepaliveHandler());//心跳
                    pipeline.addLast(new ReciveDataHandler());
                    pipeline.addLast(new MyOutboundHandler());
                    pipeline.addLast(new TCPClientHandler());

                }
            });
            // 发起连接操作
            ChannelFuture channelFuture = bootstrap.connect(nodeInfo.getIp(),
                    nodeInfo.getPort()).awaitUninterruptibly();// .sync();

            return channelFuture;

            // 等待监听端口关闭
            // channel.closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {

        }
        return null;
    }

    public void close() {
        if (channel != null)
            channel.close();
        if (bossGroup != null)
            bossGroup.shutdownGracefully();

    }


}
