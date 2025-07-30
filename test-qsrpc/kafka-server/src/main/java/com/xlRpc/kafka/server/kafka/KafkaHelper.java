package com.xlRpc.kafka.server.kafka;

import com.xl.rpc.message.Message;
import com.xl.rpc.message.MessageBuf;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class KafkaHelper {


    @Value("${kafka.producer.topic}")
    private String topic;

    @Autowired
    KafkaProducer<Object, Object> kafkaProducer;


    ThreadPoolExecutor executor;

    public long sendMsg(int threadCnt, int batchSize) {

        long startTime = System.currentTimeMillis();
        if (executor != null) {
            executor.shutdownNow();
        }
        CountDownLatch countDownLatch = new CountDownLatch(threadCnt * batchSize);
        executor = new ThreadPoolExecutor(threadCnt, threadCnt, 10,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        for (int i = 0; i < threadCnt; i++) {
            executor.execute(() -> {
                for (int j = 0; j < batchSize; j++) {
                    try {
                        MessageBuf.IMMessage imMessage = makeMessage(j + "");
                        kafkaProducer.send(new ProducerRecord<>(topic, imMessage), new Callback() {
                            @Override
                            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
//                                log.info("send msg success:{}", recordMetadata);
                            }
                        });
                    } catch (Exception e) {
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return System.currentTimeMillis() - startTime;
    }


    public static MessageBuf.IMMessage makeMessage(String content) {
        MessageBuf.IMMessage.Builder msgBuilder = MessageBuf.IMMessage.newBuilder();
        msgBuilder.setFrom(UUID.randomUUID().toString());
        msgBuilder.setTo("0098778899");
        msgBuilder.setContent(content);
        msgBuilder.setCMsgId(System.currentTimeMillis());
        msgBuilder.setType(MessageBuf.TypeEnum.ROOM_VALUE);
        msgBuilder.setSubType(MessageBuf.SubTypeEnum.ROOM_DIY_VALUE);
        msgBuilder.setDeviceId("12222222222");
        msgBuilder.setAppId("liveme");
        msgBuilder.setServerTime(System.currentTimeMillis());
        msgBuilder.setMsgId(Message.createID());
        return msgBuilder.build();
    }

}
