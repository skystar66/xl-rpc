package com.xl.rpc.reciver;


import com.xl.rpc.context.NettyContext;
import com.xl.rpc.enums.MsgType;
import com.xl.rpc.enums.NettyType;
import com.xl.rpc.message.Message;
import com.xl.rpc.server.manager.ChannelManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Description:处理收到消息接收打印
 * Date: 2018/12/10
 *
 * @author xl
 */
@ChannelHandler.Sharable
@Slf4j
@Component
public class ReciveDataHandler extends SimpleChannelInboundHandler<Message> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message cmd) throws Exception {
        //todo 先注释掉
        //        log.info("NettyType:{} cmd->{}",NettyContext.currentType(), cmd.toString());
        //心态数据包直接响应
        if (cmd.getType() ==
                (byte) MsgType.HEAT_CMD.getType()) {
            if (NettyContext.currentType().equals(NettyType.client)) {
                //设置值
                ctx.writeAndFlush(cmd);
                return;
            }
            return;
        }
        // 通知执行下一个InboundHandler
        ctx.fireChannelRead(cmd);
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        log.info("@@@@@@ channel is active！！！");
        ChannelManager.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.error("@@@@@@ channel is inActive！！！");
        ChannelManager.removeChannel(ctx.channel());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("@@@@@@ exceptionCaught：{]",cause);
        ChannelManager.removeChannel(ctx.channel());
    }
}
