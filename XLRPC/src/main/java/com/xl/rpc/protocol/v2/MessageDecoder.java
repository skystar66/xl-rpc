//package com.xl.rpc.protocol.v2;
//
//import com.leigod.im.server.common.msg.Packet;
//import com.leigod.im.server.common.msg.protobuf.MessageBuf;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.ByteToMessageDecoder;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.List;
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
// * 1              4            1           1           4           8             Uncertainty
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
//@Slf4j
//public final class MessageDecoder extends ByteToMessageDecoder {
//
//    @Override
//    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//
//        try {
//            decodeByte(in, out);
//            decodeFrames(in, out);
//        } catch (Exception exception) {
//            log.error("decode error:" +
//                    "{}", exception);
//        }
//
//
//    }
//
//    private void decodeByte(ByteBuf in, List<Object> out) {
//        while (in.isReadable()) {
//            byte b = in.readByte();
//            if (b == Packet.KEEPALIVE_BYTE) {
//                Packet keepAlivePacket = new Packet((byte) MessageBuf.PacketTypeEnum.KEEPALIVE_VALUE);
//                out.add(keepAlivePacket);
//            } else if (b == (byte) MessageBuf.PacketTypeEnum.CLIENT_KEEPALIVE_VALUE) {
//                Packet keepAlivePacket = new Packet((byte) MessageBuf.PacketTypeEnum.CLIENT_KEEPALIVE_VALUE);
//                out.add(keepAlivePacket);
//            } else {
//                in.readerIndex(in.readerIndex() - 1);
//                break;
//            }
//        }
//    }
//
//
//    private void decodeFrames(ByteBuf in, List<Object> out) {
//        if (in.readableBytes() >= Packet.HEADER_LENGTH) {
//            // 1.记录当前读取位置位置.如果读取到非完整的frame,要恢复到该位置,便于下次读取
//            in.markReaderIndex();
//
//            Packet packet = decodeFrame(in);
//            if (packet != null) {
//                out.add(packet);
//            } else {
//                // 2.读取到不完整的frame,恢复到最近一次正常读取的位置,便于下次读取
//                in.resetReaderIndex();
//            }
//        }
//    }
//
//    private Packet decodeFrame(ByteBuf in) {
//        int readableBytes = in.readableBytes();
//        int headerLength = in.readByte();
//        int bodyLength = in.readInt();
////        /**做个校验*/
////        if (ZKConfigHelper.getInstance().getImConfig().getTcpProtocolLength()<bodyLength){
////            /**body 长度超出上限*/
////            throw new IllegalArgumentException();
////        }
//        if (readableBytes < (headerLength + bodyLength)) { // bodyLength + Packet.HEADER_LENGTH
//            return null;
//        }
//        return decodePacket(in, bodyLength);
//    }
//
//    public Packet decodePacket(ByteBuf in, int bodyLength) {
//        Packet packet = new Packet();
//        packet.type = in.readByte();// read subType
//        packet.cmd = in.readByte(); // read cmd
//        packet.from = in.readLong();// read diyType
//        packet.to = in.readLong(); // read dataId
////        log.info("decode ,type:{},cmd:{},from:{},to:{},bodyLength:{}",packet.getType(),packet.cmd,packet.from,packet.to,bodyLength);
//        // read body
//        if (bodyLength > 0) {
//            in.readBytes(packet.body = new byte[bodyLength]);
//        }
//
//        return packet;
//    }
//
//}