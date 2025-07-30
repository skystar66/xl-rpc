package com.xlRpc.kafka.client.consumer;

import com.xl.rpc.message.MessageBuf;
import com.xlRpc.kafka.client.statics.StaticsManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ChatMsgConsumerHandler implements Runnable {

    private ConsumerRecords<Object, Object> consumerRecords;

    public ChatMsgConsumerHandler(ConsumerRecords<Object, Object> records) {
        this.consumerRecords = records;

    }

    @Override
    public void run() {
        for (ConsumerRecord<Object, Object> record : consumerRecords) {
            try {
                MessageBuf.IMMessage imMessage = (MessageBuf.IMMessage)record.value();
                //统计QPS、消息延时
                StaticsManager.getInstance().msgQpsIncrement();
                StaticsManager.getInstance().msgDelayReport(imMessage.getMsgId(),
                        System.currentTimeMillis() - imMessage.getServerTime());
            } catch (Exception e) {
                log.error("ChatMsgConsumerHandler error:{}", e);
            }
        }
    }
}
