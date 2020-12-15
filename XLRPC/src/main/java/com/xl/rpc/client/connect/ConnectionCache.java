package com.xl.rpc.client.connect;

import com.xl.rpc.client.RpcClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ConnectionCache {

    //节点连接池
    public static ConcurrentMap<String, String> nodePoolMap = new ConcurrentHashMap<String, String>();


    //所有节点的连接池
    public static ConcurrentMap<String, RpcClient> clientMap = new ConcurrentHashMap<String, RpcClient>();


    /**
     * 获取数量
     */
    public static int rpcPoolSize() {
        return clientMap.size();
    }

    public static RpcClient get(String key) {
        return clientMap.get(key);
    }

    public static void putIfAbsent(String key, RpcClient client) {

        clientMap.putIfAbsent(key, client);
        show();
    }

    public static void remove(String key) {
        clientMap.remove(key);
        show();
    }

    /**
     * 展示连接数量
     */
    public static void show() {
        log.info("####### 当前连接池数量: {}  连接池信息：{}", clientMap.size(),clientMap);
    }
}
