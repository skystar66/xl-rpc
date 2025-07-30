package com.xlRpc.kafka.client.consumer;

import com.xl.rpc.message.MessageBuf;
import com.xlRpc.kafka.client.statics.StaticsManager;
import com.xlRpc.kafka.client.utils.ThreadPoolUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Properties;


@Component
public class IMMsgConsumer {

    private KafkaConsumer<Object,Object> consumer;
    private Logger log = LoggerFactory.getLogger(IMMsgConsumer.class);

    @Value("${kafka.consumer.topic}")
    private String topic;

    @Value("${kafka.servers}")
    private String servers;

    @PostConstruct
    public void execute() {
        if (StringUtils.isNotBlank(topic)) {
            log.info("启动消费端:{}", topic);
            Properties props = new Properties();
            props.put("bootstrap.servers", servers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "ChatMsg-Consumer-Group" + "-localhost");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
            props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, MyMessageDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            props.put("max.poll.records", 1);
            props.put("fetch.max.bytes", Integer.MAX_VALUE - 1000);
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(topic));
            //注释 @PostConstruct 为现行执行计划，所以不能够直接的采用while循环，故启动一个线程
            new StartConsumer(consumer).start();
            log.info("topic:{},server.brokers:{},消费端初始化成功.", topic, servers);
        }
    }

    class StartConsumer extends Thread {

        private KafkaConsumer<Object,Object> consumer;

        public StartConsumer(KafkaConsumer<Object, Object> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            while (true) {
                ConsumerRecords<Object, Object> records = consumer.poll(100);
                if (records.count() > 0) {
//                    ThreadPoolUtils.getKafkaPool().execute(
//                            new ChatMsgConsumerHandler(records));

                    for (ConsumerRecord<Object, Object> record : records) {
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
        }

    }

}