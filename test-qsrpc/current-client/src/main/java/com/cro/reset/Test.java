package com.cro.reset;

import com.xl.rpc.message.MessageBuf;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        int threadNum = 100;
        int cnt = 100000;
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        CountDownLatch countDownLatch=new CountDownLatch(threadNum*cnt);
        for (int i = 0; i < threadNum; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < cnt; j++) {
                    makeMessage().toByteArray();
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(System.currentTimeMillis() - startTime);
    }

    public static MessageBuf.IMMessage makeMessage() {
        MessageBuf.IMMessage.Builder msgBuilder = MessageBuf.IMMessage.newBuilder();
        msgBuilder.setFrom("1L");
        msgBuilder.setTo("0098778899");
        msgBuilder.setContent("12321321312sddasdas");
        msgBuilder.setCMsgId(System.currentTimeMillis());
        msgBuilder.setType(MessageBuf.TypeEnum.ROOM_VALUE);
        msgBuilder.setSubType(MessageBuf.SubTypeEnum.ROOM_DIY_VALUE);
        msgBuilder.setDeviceId("12222222222");
        msgBuilder.setAppId("liveme");
        msgBuilder.setServerTime(System.currentTimeMillis());
//        msgBuilder.setMsgId(Message.createID());
        return msgBuilder.build();
    }
}
