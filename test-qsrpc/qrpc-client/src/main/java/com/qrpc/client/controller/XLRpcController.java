package com.qrpc.client.controller;

import com.qrpc.api.ApiServer;
import com.xl.rpc.starter.client.XLRpcReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("xlrpc")
public class XLRpcController {


    @XLRpcReference(value = "apiServer",version = "1.0")
     ApiServer rpcServer;

    @ResponseBody
    @RequestMapping(value = "/hello",method = RequestMethod.GET)
    public String hello() {
        return rpcServer.hello("Welcome Use XL-RPC Framework !!!")+
                rpcServer.req("+++req");
    }
}
