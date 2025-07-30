package com.xlRpc.kafka.server.config;

import com.xl.rpc.message.MessageBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class MyMessageDeserializer implements Deserializer<MessageBuf.IMMessage> {


    @Override
    public MessageBuf.IMMessage deserialize(String s, byte[] bytes) {
        try {
            return MessageBuf.IMMessage.parseFrom(bytes);
        } catch (Exception e) {
            log.error("deserialize err:", e);
        }
        return null;
    }
}
