package com.xlRpc.grpc.client;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author xuliang
 */
@Slf4j
public class GrpcClient {





    /**
     * 初始化
     */
    public static ManagedChannel getChannel(String ip, int port) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port)
                .usePlaintext()
//                .defaultLoadBalancingPolicy("round_robin")
                .keepAliveWithoutCalls(true)
                .keepAliveTime(10, TimeUnit.SECONDS)
                .keepAliveTimeout(3, TimeUnit.SECONDS)
                .enableRetry()
                .retryBufferSize(1024 * 1024)

                .build();

        return channel;


    }


}
