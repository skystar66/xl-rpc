package com.contr;

import com.xl.rpc.starter.enable.EnableQSRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableQSRpc(qps = 100000) 限制整个服务qps
@SpringBootApplication
@EnableQSRpc(enabledServer = false)
public class ServerRPCApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerRPCApplication.class, args);
    }
}
