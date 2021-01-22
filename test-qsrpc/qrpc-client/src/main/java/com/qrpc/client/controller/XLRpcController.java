package com.qrpc.client.controller;

import com.qrpc.api.ApiServer;
import com.xl.rpc.starter.client.XLRpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("xlrpc")
@Slf4j
public class XLRpcController {


    @XLRpcReference(value = "apiServer",version = "1.0")
     ApiServer rpcServer;


    private static ExecutorService executorService = Executors.newFixedThreadPool(100);


    @ResponseBody
    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    public String hello() {

        AtomicInteger atomicInteger = new AtomicInteger(0);

        StringBuffer result = new StringBuffer();
        CountDownLatch countDownLatch = new CountDownLatch(200);
        for (int i=0;i<200;i++) {

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    result.append(rpcServer.hello("Welcome Use XL-RPC Framework !!!")+
                            rpcServer.req("+++req"));
                    log.info("####### 当前消息数：{}",atomicInteger.incrementAndGet());
//                    countDownLatch.countDown();
                }
            });
        }
//        try {
//            countDownLatch.await();
//        }catch (Exception ex) {
//
//        }
        return result.toString();
    }
}
