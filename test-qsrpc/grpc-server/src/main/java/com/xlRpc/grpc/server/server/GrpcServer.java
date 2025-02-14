package com.xlRpc.grpc.server.server;

import com.xlRpc.grpc.server.service.ProcessService;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class GrpcServer {


    private Server server;


    public void start() {
        try {
            server = NettyServerBuilder
                    .forPort(50001)
                    .addService(new ProcessService())
                    .handshakeTimeout(3, TimeUnit.SECONDS)
                    .permitKeepAliveWithoutCalls(true)
                    .permitKeepAliveTime(1, TimeUnit.SECONDS)
                    .keepAliveTime(10, TimeUnit.SECONDS)
                    .keepAliveTimeout(3, TimeUnit.SECONDS)
                    .bossEventLoopGroup(null)
                    .workerEventLoopGroup(null)
                    .build();
            server.start();
            System.out.println("GrpcServer init,Port: "+server.getPort());
            log.info("GrpcServer init,Port:{}", server.getPort());
            server.awaitTermination();
        } catch (Exception e) {
            log.error("grpc server start failed, e:", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
            log.info("grpc server stop");
        }
    }


}
