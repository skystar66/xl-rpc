package com.xl.rpc.utils;

import io.netty.util.AttributeKey;

public interface AttributeKeys {


    /**
     * 连接池连接标记key
     */
    AttributeKey<String> RPC_POOL_KEY = AttributeKey.valueOf("RpcPoolKey");

    /**
     * RPC服务器IP
     */
    AttributeKey<String> RPC_SERVER = AttributeKey.valueOf("RpcServer");

    /**
     * Rpc Port
     */
    AttributeKey<Integer> RPC_PORT = AttributeKey.valueOf("RpcPort");

    /**
     * Rpc ChannelId
     */
    AttributeKey<String> RPC_CHANNELID = AttributeKey.valueOf("RpcChannelId");

    /**
     * RPC连接编号
     */
    AttributeKey<Integer> RPC_INDEX = AttributeKey.valueOf("RpcIndex");

}
