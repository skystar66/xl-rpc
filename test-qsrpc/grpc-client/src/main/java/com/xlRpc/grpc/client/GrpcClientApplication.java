package com.xlRpc.grpc.client;

import com.xl.rpc.message.Message;
import com.xl.rpc.message.MessageBuf;
import com.xlRpc.grpc.common.rpc.CommonServiceGrpc;
import com.xlRpc.grpc.common.rpc.GrpcServerService;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GrpcClientApplication {
    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);


    private static ScheduledExecutorService schedule = Executors.newScheduledThreadPool(1);

    private final static int PORT = 10086;
    private final static int count = 62500;// 8 * 125000=100万请求
    //        private final static int count = 1;//
    private final static int thread = DEFAULT_THREAD_POOL_SIZE;//x个请求线程
    private final static long totalReqCount = count * thread;//总共请求
    //    private final static long totalReqCount = 1000000;//总共请求
    private final static String zip = "";//gzip snappy
    private final static int timeout = 10_000;
    static AtomicInteger reqCnt = new AtomicInteger(0);

    static List<ManagedChannel> channels = new CopyOnWriteArrayList<>();

    static int size = 8;

    static Random random = new Random();

    static Integer cnt = 0;

    public static void main(String[] args) {

        initChannel();
        schedule.scheduleAtFixedRate(() -> {
            printRequestMap();
        }, 0, 1, TimeUnit.SECONDS);


        CountDownLatch countDownLatch = new CountDownLatch(count * thread);
        long start = System.currentTimeMillis();

        AtomicInteger failedCnt = new AtomicInteger(0);


//        ManagedChannel channel = GrpcClient.getChannel("127.0.0.1", 50001);

        for (int i = 0; i < thread; i++) {
            //160万并发：4-core-> use time:116627ms ,qps:13718个 ,流量:1714KB/s ,平均请求延时:0ms
            //100万并发：4-core-> use time:75253ms ,qps:13288个 ,流量:1661KB/s ,平均请求延时:0ms
            EXECUTOR_SERVICE.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < count; j++) {

                        try {

                            ManagedChannel channel = getChannel();
                            Message msg = new Message();
                            msg.setContent(makeMessage().toByteArray());
                            CommonServiceGrpc.CommonServiceBlockingStub stub = CommonServiceGrpc.newBlockingStub(channel)
                                    .withDeadlineAfter(60, TimeUnit.MINUTES);
                            GrpcServerService.Response handle = stub.handle(GrpcServerService.Request.newBuilder().setRequestId(
                                            msg.getId()
                                    )
                                    .setRequest(makeMessage().toByteString()).build());
                            reqCnt.incrementAndGet();
                        } catch (Exception e) {
                            failedCnt.incrementAndGet();
                            e.printStackTrace();
                        } finally {
//                            if (channel != null) {
//                                channel.shutdown();
//                            }
                            countDownLatch.countDown();
                        }

                    }
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        long use = System.currentTimeMillis() - start;
        long qps = totalReqCount * 1000 / use;
        System.err.println(Runtime.getRuntime().availableProcessors()
                + "-core-> " + totalReqCount + "请求 -> use time:" + use + "ms" +
                " ,qps:" + qps + "/s" +
                " ,错误数:" + failedCnt.get() +
                " ,错误率:" + (failedCnt.get() / totalReqCount) * 100 + "%" +
                " ,平均请求延时:" + (use / totalReqCount) + "ms");
    }


    public static MessageBuf.IMMessage makeMessage() {
        MessageBuf.IMMessage.Builder msgBuilder = MessageBuf.IMMessage.newBuilder();
        msgBuilder.setFrom(UUID.randomUUID().toString());
        msgBuilder.setTo("0098778899");
        msgBuilder.setContent("12321321312sddasdas");
        msgBuilder.setCMsgId(System.currentTimeMillis());
        msgBuilder.setType(MessageBuf.TypeEnum.ROOM_VALUE);
        msgBuilder.setSubType(MessageBuf.SubTypeEnum.ROOM_DIY_VALUE);
        msgBuilder.setDeviceId("12222222222");
        msgBuilder.setAppId("liveme");
        return msgBuilder.build();
    }


    static ManagedChannel getChannel() {
        if (!CollectionUtils.isEmpty(channels)) {
            int ix = random.nextInt(size) - 1;
            if (ix < 0) {
                ix = 0;
            }
            return channels.get(ix);
        }
        for (int i = 0; i < size; i++) {
            ManagedChannel channel = GrpcClient.getChannel("127.0.0.1", 50001);
            channels.add(channel);
        }
        return channels.get(0);
    }


    static ManagedChannel initChannel() {
        for (int i = 0; i < size; i++) {
            ManagedChannel channel = GrpcClient.getChannel("127.0.0.1", 50001);
            channels.add(channel);
        }
        return channels.get(0);
    }

    static void printRequestMap() {
        cnt++;
        System.out.println(">>>>>>>>>> 播报次数：" + cnt + " reqCnt:" + reqCnt.getAndSet(0) + "/s");

    }

}