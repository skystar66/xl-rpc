package com.xlRpc.grpc.server;

import com.xlRpc.grpc.server.server.GrpcServer;
import io.netty.buffer.PooledByteBufAllocator;

public class GrpcServerApplication {
    public static void main(String[] args) {



        // 获取默认的 PooledByteBufAllocator 实例
        PooledByteBufAllocator allocator = PooledByteBufAllocator.DEFAULT;


        GrpcServer grpcServer = new GrpcServer();

        grpcServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                grpcServer.stop();
            }
        }));

    }
}