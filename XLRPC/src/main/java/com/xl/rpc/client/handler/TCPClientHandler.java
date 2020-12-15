package com.xl.rpc.client.handler;

import com.xl.rpc.callback.Callback;
import com.xl.rpc.callback.CallbackPool;
import com.xl.rpc.client.connect.NodePoolCache;
import com.xl.rpc.enums.MsgType;
import com.xl.rpc.message.Message;
import com.xl.rpc.mq.MQProvider;
import com.xl.rpc.utils.AttributeKeys;
import com.xl.rpc.utils.SnowflakeIdWorker;
import com.xl.rpc.zk.NodeInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.time.Duration;

/**
 * @author xuliang
 * @date 2020年12月09日 下午2:04:37
 * <p>
 * tcp连接类nio回调类
 */
public class TCPClientHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(TCPClientHandler.class);
    private Message heartCmd;


    /**
     * 构造心跳消息
     */
    public TCPClientHandler() {
        heartCmd = new Message();
        heartCmd.setId(SnowflakeIdWorker.getInstance().nextId().intValue());
        heartCmd.setType((byte) MsgType.HEAT_CMD.getType());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {

        //System.out.println("TCPRouteHandler-HandlerMessage:" + msg.getId() + "-" + Thread.currentThread().getName());
        @SuppressWarnings("unchecked")
        Callback<Message> cb = (Callback<Message>) CallbackPool.remove(msg.getId());
        if (cb == null) {
            //找不到回调//可能超时被清理了
            logger.warn("Receive msg from server but no context found, requestId=" + msg.getId() + "," + ctx);
            return;
        }
        cb.handleResult(msg);
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("channelInactive(" + ctx + ")");
        String rpcServer = ctx.channel().attr(AttributeKeys.RPC_SERVER).get();
        Integer rpcPort = ctx.channel().attr(AttributeKeys.RPC_PORT).get();
        Integer rpcIndex = ctx.channel().attr(AttributeKeys.RPC_INDEX).get();

        String localAddress = ctx.channel().localAddress().toString();
        String remoteAddress = ctx.channel().remoteAddress().toString();
        logger.info("连接非活动!!!! rpcServer={}, rpcPort={}, channel={}, localAddress={}", rpcServer, rpcPort, ctx.channel(), localAddress);

        closeChannel(ctx);
        //解决IP为0.0.0.0/0.0.0.0:33703的问题
        if(localAddress.startsWith("0.0.0.0") || remoteAddress.startsWith("0.0.0.0")){
            //停止
            logger.error("localAddress={} 为无效地址, 停止重连!", localAddress);
        }else{
            logger.info("开始执行重连业务...");
            //重连连接
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setRpcServerIndex(rpcIndex);
            nodeInfo.setIp(rpcServer);
            nodeInfo.setPort(rpcPort);
            nodeInfo.setRetrySize(3);
            MQProvider.getRetryConnectQueue().push(nodeInfo, Duration.ofMillis(500));

        }
    }

    private void closeChannel(ChannelHandlerContext ctx) throws Exception {
        //清除map中连接信息
        String rpcPoolIndex = ctx.channel().attr(AttributeKeys.RPC_POOL_KEY).get() ;
        String rpcServer = ctx.channel().attr(AttributeKeys.RPC_SERVER).get();
        NodePoolCache.removeActionRpcSrv(rpcServer,rpcPoolIndex);
        logger.info("清除rpcPoolIndex={}", rpcPoolIndex);
        ctx.channel().close().sync();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("exceptionCaught(" + ctx + ")", cause);
        if (cause instanceof ConnectException) {
            String rpcServer = ctx.channel().attr(AttributeKeys.RPC_SERVER).get();
            Integer rpcPort = ctx.channel().attr(AttributeKeys.RPC_PORT).get();
            Integer rpcIndex = ctx.channel().attr(AttributeKeys.RPC_INDEX).get();
            String rpcPoolKey = ctx.channel().attr(AttributeKeys.RPC_POOL_KEY).get() ;
            Thread.sleep(1000 * 15);
            logger.error("try connect tx-manager:{} ", ctx.channel().remoteAddress());
            NodePoolCache.removeActionRpcSrv(rpcServer,rpcPoolKey);

            //重连连接
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setRpcServerIndex(rpcIndex);
            nodeInfo.setIp(rpcServer);
            nodeInfo.setPort(rpcPort);
            nodeInfo.setRetrySize(3);
            MQProvider.getRetryConnectQueue().push(nodeInfo, Duration.ofMillis(500));

        }
        /**发送心跳探活*/
        ctx.channel().writeAndFlush(heartCmd);
    }

}
