package com.qrpc.server.impl;

import com.qrpc.api.ApiServer;
import com.xl.rpc.starter.server.XLRpcService;

@XLRpcService(value = "apiServer",version = "1.0") //设置版本号及该服务qps
public class ApiServerImpl implements ApiServer {

    public String hello(String name) {
        return name;
    }
}
