package com.xlRpc.kafka.server.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaProducerConfig {


    @Value("${kafka.servers}")
    private String zkServers;

    @Bean
    public KafkaProducer<Object, Object> kafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", zkServers);
        props.put("client.id", "Task-Chat-Producer");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", MyMessageSerializer.class.getName());
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000);
        props.put("producer.type", "async");
        KafkaProducer<Object, Object> producer = new KafkaProducer<>(props);
        log.info("init kafka producer!");
        return producer;
    }


}
