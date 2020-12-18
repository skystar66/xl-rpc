package com.xl.rpc.starter.server.starter;

import com.google.common.util.concurrent.RateLimiter;
import com.xl.rpc.client.starter.ClientStarter;
import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.server.ServerStarter;
import com.xl.rpc.server.node.NodeBuilder;
import com.xl.rpc.starter.common.serialize.ISerialize;
import com.xl.rpc.starter.common.serialize.Protostuff;
import com.xl.rpc.starter.common.utils.CGlib;
import com.xl.rpc.starter.dto.Request;
import com.xl.rpc.starter.dto.Response;
import com.xl.rpc.starter.enable.EnableQSRpc;
import com.xl.rpc.starter.server.cache.CacheResponse;
import com.xl.rpc.starter.server.context.RpcServiceContext;
import com.xl.rpc.zk.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author Created by xl
 * Date 2020/11/11
 * 启动rpc服务及消息处理
 */
public class RpcServiceStarter {


    // 内部静态类方式
    private static class InstanceHolder {
        private static RpcServiceStarter instance = new RpcServiceStarter();
    }

    public static RpcServiceStarter getInstance() {
        return RpcServiceStarter.InstanceHolder.instance;
    }


    private ISerialize iSerialize;

    private CacheResponse cacheResponse;
    private Map<String, RpcServiceContext> contextMap;
    private RateLimiter rateLimiter;

    /**
     * 初始化数据
     */
    public void init(Map<String, RpcServiceContext> contextMap, EnableQSRpc enableQSRpc) {
        this.contextMap = contextMap;
        if (iSerialize == null) iSerialize = new Protostuff();
        cacheResponse = new CacheResponse(iSerialize);
        if (enableQSRpc.qps() > 0) {
            /**针对整个服务做限流*/
            rateLimiter = RateLimiter.create(enableQSRpc.qps());
        }
    }


    /**
     * 启动服务
     */
    public void start() {
        NodeInfo nodeInfo = NodeBuilder.buildNode();//read application.properties
        String[] actions = new String[contextMap.size()];
        contextMap.keySet().toArray(actions);
        nodeInfo.setActions(actions);
        new Thread(new ServerStarter(nodeInfo,new MessageListener() {
            @Override
            public byte[] onMessage(byte[] message) {
                /**针对整个服务做限制qps*/
                if (rateLimiter != null) {
                    if (!rateLimiter.tryAcquire()) {
                        return cacheResponse.qpsLimit();
                    }
                }
                try {
                    return onHandle(message);
                } catch (Throwable e) {
                    e.printStackTrace();
                    //获取业务抛出的异常
                    if (e instanceof InvocationTargetException && e.getCause() != null) e = e.getCause();

                    String msg = e.getMessage();
                    if (msg == null || msg.isEmpty()) msg = e.toString();
                    Response err = new Response();
                    //统一返回Exception,防止客户端没有这个错误类,序列化失败
                    err.setException(new Exception(msg));
                    return iSerialize.serialize(err);
                }
            }
        })).start();
    }


    /**
     * 处理rpc消息
     */
    private byte[] onHandle(byte[] message) throws InvocationTargetException {
        Request request = iSerialize.deserialize(message, Request.class);
        RpcServiceContext serviceContext = contextMap.get(request.getInterfaceName() + request.getVersion());
        if (serviceContext == null) {
            return cacheResponse.nofound();
        }

        /**限制 具体的 rpcServer 接口的 qps*/
        if (serviceContext.rateLimiter != null) {
            if (!serviceContext.rateLimiter.tryAcquire()) {
                return cacheResponse.qpsLimit();
            }
        }
        /**获取服务动态代理对象*/
        Object obj = CGlib.invoke(serviceContext.object, serviceContext.methodMap.get(
                request.getMethodName()),
                request.getParamters());
        if (obj == null) {
            return cacheResponse.empty();
        }
        Response response = new Response();
        response.setResult(obj);
        return iSerialize.serialize(response);
    }


}
