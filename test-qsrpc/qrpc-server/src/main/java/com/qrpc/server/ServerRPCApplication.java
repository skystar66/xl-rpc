package com.qrpc.server;

import com.xl.rpc.starter.enable.EnableQSRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableQSRpc(enabledServer = true)// 开启服务端模式
//@EnableQSRpc(qps = 100000) 限制整个服务qps
@SpringBootApplication
public class ServerRPCApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerRPCApplication.class, args);
    }
}
