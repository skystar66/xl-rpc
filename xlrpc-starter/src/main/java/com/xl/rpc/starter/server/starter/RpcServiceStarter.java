package com.xl.rpc.starter.server.starter;

import com.google.common.util.concurrent.RateLimiter;
import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.server.ServerStarter;
import com.xl.rpc.server.node.NodeBuilder;
import com.xl.rpc.starter.common.serialize.ISerialize;
import com.xl.rpc.starter.common.serialize.Protostuff;
import com.xl.rpc.starter.common.utils.CGlib;
import com.xl.rpc.starter.dto.Request;
import com.xl.rpc.starter.dto.Response;
import com.xl.rpc.starter.enable.EnableXLRpc;
import com.xl.rpc.starter.server.cache.CacheResponse;
import com.xl.rpc.starter.server.context.RpcServiceContext;
import com.xl.rpc.zk.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Created by xl
 * Date 2020/11/11
 * 启动rpc服务及消息处理
 */
public class RpcServiceStarter {

    private static Logger logger = LoggerFactory.getLogger(RpcServiceStarter.class);

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
    public void init(Map<String, RpcServiceContext> contextMap, EnableXLRpc enableXLRpc) {
        this.contextMap = contextMap;
        if (iSerialize == null) iSerialize = new Protostuff();
        cacheResponse = new CacheResponse(iSerialize);
        if (enableXLRpc.qps() > 0) {
            /**针对整个服务做限流 每秒允许最大qps 为qps*/
            rateLimiter = RateLimiter.create(enableXLRpc.qps(),1, TimeUnit.SECONDS);
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
        Long start = System.currentTimeMillis();
        Request request = iSerialize.deserialize(message, Request.class);

//        String serverName = request.getInterfaceName() + request.getVersion();

        String serverName = request.getServiceBeanName();
        logger.info("Server Invoke Recive Client reqId: {} | serviceBeanName: {} | ReqParams: {}"
                ,request.getReqId(),serverName, request.getParamters());

        RpcServiceContext serviceContext = contextMap.get(serverName);
        if (serviceContext == null) {
            return cacheResponse.nofound();
        }

        /**限制 具体的 rpcServer 接口的 qps*/
        if (serviceContext.rateLimiter != null) {
            if (!serviceContext.rateLimiter.tryAcquire()) {
                logger.info("reqId: {} | serviceBeanName: {} | QPS Limit Is Intercepted！QPSCount: {} " +
                                "Invoke Recive Client ReqParams: {}"
                        ,request.getReqId(),serverName,serviceContext.qps,
                         request.getParamters());
                return cacheResponse.qpsLimit();
            }
        }

        /**获取服务动态代理对象*/
        Object obj = CGlib.invoke(serviceContext.object, serviceContext.methodMap.get(
                request.getMethodName()),
                request.getParamters());
        logger.info("reqId: {} | Cost Time: {}ms serviceBeanName: {} Invoke Rpc Server" +
                        " Response:【{}】 | ReqParams: {}"
                ,request.getReqId(), System.currentTimeMillis()-start,
                serverName,
                obj,
                request.getParamters());
        Response response = new Response();
        response.setResult(obj);
        response.setReqId(request.getReqId());
        return iSerialize.serialize(response);
    }


}
