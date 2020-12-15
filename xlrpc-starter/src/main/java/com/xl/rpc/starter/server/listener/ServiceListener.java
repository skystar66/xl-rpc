package com.xl.rpc.starter.server.listener;

import com.google.common.util.concurrent.RateLimiter;
import com.xl.rpc.starter.common.utils.AopTargetUtils;
import com.xl.rpc.starter.enable.EnableQSRpc;
import com.xl.rpc.starter.server.XLRpcService;
import com.xl.rpc.starter.server.context.RpcServiceContext;
import com.xl.rpc.starter.server.starter.RpcServiceStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * RPC 服务器（用于发布 RPC 服务）
 */
@Service
public class ServiceListener implements ApplicationListener<ContextRefreshedEvent> {
    private Map<String, RpcServiceContext> contextMap = new HashMap<>();

    private EnableQSRpc enableQSRpc;

    private static Logger logger = LoggerFactory.getLogger(ServiceListener.class);

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        scanRpcService(contextRefreshedEvent.getApplicationContext());
    }
    private void scanRpcService(ApplicationContext applicationContext) {

        Map<String, Object> startMap = applicationContext.getBeansWithAnnotation(EnableQSRpc.class);
        if (startMap == null) return;

        enableQSRpc = startMap.values().iterator().next().getClass().getAnnotation(EnableQSRpc.class);
        /**校验开关是否开启*/
        if (!enableQSRpc.enabledServer()) return;
        /**获取rpcservice 注解服务*/
        Map<String, Object> rpcServiceMap = applicationContext.getBeansWithAnnotation(XLRpcService.class);
        if (rpcServiceMap.isEmpty()) return;//没有提供rpc服务
        for (Object bean : rpcServiceMap.values()) {

            Object serviceBean = bean;
            try {
                //获取代理对象
                serviceBean = AopTargetUtils.getTarget(bean);
            } catch (Exception e) {
                e.printStackTrace();
            }

            XLRpcService qsRpcService = serviceBean.getClass().getAnnotation(XLRpcService.class);
            if (qsRpcService == null) continue;

            Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
            if (interfaces == null || interfaces.length == 0)
                throw new IllegalArgumentException(serviceBean + " @XLRpcService 必须实现一个接口");

            for (Class i : interfaces) {
                String serviceName = i.getName();
                String serviceVersion = qsRpcService.value();
                if (serviceVersion.isEmpty()) serviceVersion = qsRpcService.version();
                if (!serviceVersion.isEmpty()) {
                    serviceName += serviceVersion;
                }
                RpcServiceContext rpcServiceContext = new RpcServiceContext();
                rpcServiceContext.object = serviceBean;
                Method[] methods = i.getMethods();
                for (Method m : methods) {
                    rpcServiceContext.methodMap.put(m.toString(), m);
                }
                float qps = qsRpcService.qps();
                if (qps > 0) rpcServiceContext.rateLimiter = RateLimiter.create(qps);
                contextMap.put(serviceName, rpcServiceContext);
            }
        }
        run();
    }

    private void run() {
        if (contextMap.isEmpty()) {
            logger.info("没有发现@QSRpcService服务>_<");
            return;
        }
        /**初始化服务信息*/
        RpcServiceStarter.getInstance().init(contextMap, enableQSRpc);
        /**启动服务端*/
        RpcServiceStarter.getInstance().start();
        logger.info("QSRPC节点服务已启动^_^:" + contextMap.keySet());
    }


}
