package com.xlRpc.kafka.server.config;

import com.xl.rpc.message.MessageBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class MyMessageSerializer implements Serializer<MessageBuf.IMMessage> {

    @Override
    public byte[] serialize(String s, MessageBuf.IMMessage message) {
        try {
            return message.toByteArray();
        } catch (Exception e) {
            log.error("serialize err:", e);
        }
        return null;
    }
}
