package com.xl.rpc.protocol;

import com.xl.rpc.message.Message;
import com.xl.rpc.zip.IZip;
import com.xl.rpc.zip.Zip;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xl
 * @date 2018年11月22日 上午11:55:38
 * <p>
 * 适用短连接/追求效率+比较可靠的长连接 包头长度+特定包尾 一旦丢包必须关闭重连,因为后面数据会错乱
 * <p>
 * 长度(4)包id(4)版本号(1)压缩类型(1)消息类型(1)内容(n)包尾(2)
 *
 * 发送 编码
 */
@Slf4j
public class MessageEncoder extends MessageToByteEncoder<Message> {

    public final static byte[] BYTE_END = new byte[]{'\r', '\n'};


    @Override
    public void encode(ChannelHandlerContext ctx, Message in, ByteBuf out) throws Exception {

        long time = System.currentTimeMillis();

        //1000分之一的打印机会
        if (time % 1000 == 0) {
//            log.error("NettyType : {} send->{}", NettyContext.currentType(),
//                in.toString());
        }


        byte[] content = in.getContent();
        if (content != null) {
            //判断是否需要压缩
            IZip iZip = Zip.get(in.getZip());
            if (iZip != null) content = iZip.compress(content);
            out.writeInt(content.length + 9);
        }else {
            out.writeInt(9);
        }

        out.writeByte(in.getType());

        /**公共参数*/
        out.writeInt(in.getId());
        out.writeByte(in.getVer());

        //ver=0编码逻辑,后续更新通信版本需要区分处理逻辑
        out.writeByte(in.getZip());
        if (content != null) {
            out.writeBytes(content);
        }
        out.writeBytes(BYTE_END);


    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("MessageEncoder is error:{}",cause);
    }

}
