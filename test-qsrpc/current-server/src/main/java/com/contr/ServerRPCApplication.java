package com.contr;

import com.xl.rpc.server.queue.unlock.ServerMsgFastQueueConsumer;
import com.xl.rpc.starter.enable.EnableXLRpc;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableQSRpc(qps = 100000) 限制整个服务qps
@SpringBootApplication
@EnableXLRpc(enabledServer = false)
public class ServerRPCApplication implements CommandLineRunner {


    public static void main(String[] args) {
        System.setProperty("io.netty.allocator.type", "pooled");
//        System.setProperty("io.netty.maxDirectMemory", "2g");
        SpringApplication.run(ServerRPCApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ServerMsgFastQueueConsumer.getInstance().start();
    }
}
