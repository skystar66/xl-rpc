package com.qrpc.server.impl;

import com.qrpc.api.ApiServer;
import com.xl.rpc.starter.server.XLRpcService;

/**
 * 具体的服务提供类
 * 设置版本号及该服务qps50
 *
 * @author xl
 * @date 2020-12-18
 */
@XLRpcService(value = "apiServer", version = "1.0", qps = 50)
public class ApiServerImpl implements ApiServer {

    public String hello(String name) {
        return name;
    }


    @Override
    public String req(String req) {
        return req;
    }
}
