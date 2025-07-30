package com.xlRpc.kafka.server.controller;

import com.xlRpc.kafka.server.kafka.KafkaHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("kafka")
@RestController
@Slf4j
public class KafkaTestController {


    @Autowired
    KafkaHelper kafkaHelper;

    @RequestMapping("/test")
    public String test(@RequestParam("threadCnt") int threadCnt,
                       @RequestParam("batchSize") int batchSize) {
        long sendMsg = kafkaHelper.sendMsg(threadCnt, batchSize);
        return "共处理" + threadCnt * batchSize + "条数据，共耗时:" + sendMsg + "ms.";
    }


}
