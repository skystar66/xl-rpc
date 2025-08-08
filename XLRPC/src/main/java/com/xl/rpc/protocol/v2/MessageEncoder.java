//package com.xl.rpc.protocol.v2;
//
//import com.leigod.im.server.common.msg.Packet;
//import com.leigod.im.server.common.msg.protobuf.MessageBuf;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandler;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.MessageToByteEncoder;
//
//
///**
// * 消息解码
// *
// * @author xuliang
// * @version 2017年10月16日
// * <p>
// * Protocol
// * __ __ __ __ __ __ __ __ __ ____ __ __ __ __ __ __ ____ __ __ _____ __ __ ____ __ __ __ __ __ __ __ __
// * |              |              |           |           |           |           |                         |
// * 1              4            1           1           4           4             Uncertainty
// * |__ __ __ __ __|__ __ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|_ __ __ __ __ __ __ __ __|
// * |              |              |           |           |           |           |                         |
// * HeaderLength    BodyLength       Cmd       SubType     DiyType      DataId          BodyContent
// * |__ __ __ __ __|__ __ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ ____ __ __ __ __|
// * <p>
// * 协议头15个字节定长
// * HeaderLength//byte  :包头长，
// * BodyLength  //int   :包体长，
// * Cmd         //byte  :同消息type类型
// * SubType     //byte  :同消息subType类型
// * DiyType     //int   :直播间自定义消息类型对应数字，如:App:@TXT=1
// * DataId      //long   :单聊/群聊传cMsgId，直播间为roomId的HashCode
// * Body 	   //byte[]:协议内容
// * 注：心跳需特殊处理，只传输1个字节的空包，值为-99，无需包头和包体
// */
//@ChannelHandler.Sharable
//public final class MessageEncoder extends MessageToByteEncoder<Packet> {
//
//    public static final MessageEncoder INSTANCE = new MessageEncoder();
//
//    @Override
//    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
//
//
//        if (packet.type == MessageBuf.PacketTypeEnum.KEEPALIVE_VALUE) {          //packet.cmd == Packet.KEEPALIVE_BYTE
//            out.writeByte(Packet.KEEPALIVE_BYTE);
//            packet.body = null;
//        } else if (packet.type == (byte) MessageBuf.PacketTypeEnum.CLIENT_KEEPALIVE_VALUE) {          //packet.cmd == Packet.KEEPALIVE_BYTE
//            out.writeByte(MessageBuf.PacketTypeEnum.CLIENT_KEEPALIVE_VALUE);
//            //如果是单聊，释放内存
//            packet.body = null;
//        } else {
//            out.writeByte(Packet.HEADER_LENGTH);
//            out.writeInt(packet.getBodyLength());
//            out.writeByte(packet.type);
//            out.writeByte(packet.cmd);
//            out.writeLong(packet.from);
//            out.writeLong(packet.to);
//            if (packet.getBodyLength() > 0) {
//                out.writeBytes(packet.body);
//            }
//            if (packet.type != (byte) MessageBuf.PacketTypeEnum.LSB_VALUE) {
//                //解决 MessageBuf.PacketTypeEnum.LSB_VALUE + RPC_PULL_GATEWAY_METRICS_VALUE 回包的空指针问题
//                packet.body = null;
//            }
//        }
//        //packet.body = null;
//    }
//}
