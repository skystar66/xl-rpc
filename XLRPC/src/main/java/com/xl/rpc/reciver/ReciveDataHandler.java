package com.xl.rpc.reciver;


import com.xl.rpc.context.NettyContext;
import com.xl.rpc.enums.MsgType;
import com.xl.rpc.enums.NettyType;
import com.xl.rpc.message.Message;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Description:
 * Company: CodingApi
 * Date: 2018/12/10
 *
 * @author ujued
 */
@ChannelHandler.Sharable
@Slf4j
@Component
public class ReciveDataHandler extends SimpleChannelInboundHandler<Message> {



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message cmd) throws Exception {
        log.info("NettyType:{} cmd->{}",NettyContext.currentType(), cmd.toString());
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
}
