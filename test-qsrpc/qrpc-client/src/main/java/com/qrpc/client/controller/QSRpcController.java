//package com.qrpc.client.controller;
//
//import com.qrpc.api.ApiServer;
//import com.xl.rpc.starter.client.XLRpcReference;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("qsrpc")
//public class QSRpcController {
//
//
//    @XLRpcReference(value = "apiServer",version = "1.0")
////@QSRpcReference(version = "2.0",timeout = 10000) 配置版本号及超时
//     ApiServer rpcServer;
//
//    @ResponseBody
//    @RequestMapping(value = "/hello",method = RequestMethod.GET)
//    public String hello() {
//        return rpcServer.hello("QSPRC");
//    }
//}
