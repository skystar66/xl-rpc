package com.qrpc.server.impl;

import com.qrpc.api.OrderServer;
import com.xl.rpc.starter.server.XLRpcService;

/**
 * 具体的服务提供类
 * 设置版本号及该服务qps50
 *
 * @author xl
 * @date 2020-12-18
 */
@XLRpcService(value = "orderServer", version = "1.0", qps = 50)
public class OrderServerImpl implements OrderServer {


    @Override
    public String order(String orderId) {
        return orderId;
    }
}
