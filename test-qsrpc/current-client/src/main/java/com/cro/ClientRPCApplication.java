package com.cro;

import com.xl.rpc.starter.enable.EnableXLRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableQSRpc(qps = 100000) 限制整个服务qps
@EnableXLRpc(enabledClient = false)
@SpringBootApplication
public class ClientRPCApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientRPCApplication.class, args);
    }
}
