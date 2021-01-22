package com.xl.rpc.starter.server.listener;

import com.google.common.util.concurrent.RateLimiter;
import com.xl.rpc.starter.common.exc.DuplicateException;
import com.xl.rpc.starter.common.utils.AopTargetUtils;
import com.xl.rpc.starter.common.utils.BeanNameUtil;
import com.xl.rpc.starter.enable.EnableQSRpc;
import com.xl.rpc.starter.server.XLRpcService;
import com.xl.rpc.starter.server.context.RpcServiceContext;
import com.xl.rpc.starter.server.starter.RpcServiceStarter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * RPC 服务器（用于发布 RPC 服务）
 */
@Service
public class ServiceListener implements ApplicationListener<ContextRefreshedEvent> {
    private Map<String, RpcServiceContext> contextMap = new HashMap<>();

    private EnableQSRpc enableQSRpc;


    private static Logger logger = LoggerFactory.getLogger(ServiceListener.class);


    private static ScheduledExecutorService printScheduledExecutorService
            = Executors.newScheduledThreadPool(2);


    @SneakyThrows
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        scanRpcService(contextRefreshedEvent.getApplicationContext());
    }

    private void scanRpcService(ApplicationContext applicationContext) throws Exception {

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
                /**获取全路径包名*/
                String interfaceServiceName = i.getName();
                /**获取服务注册名*/
                String serviceBeanName = qsRpcService.value();
                String serviceVersion = qsRpcService.version();
//                if (serviceVersion.isEmpty()) serviceVersion = qsRpcService.version();
//                if (!serviceVersion.isEmpty()) {
//                    serviceName += serviceVersion;
//                }

                serviceBeanName = BeanNameUtil.getInstance().getServiceBeanName(
                        interfaceServiceName,serviceBeanName,serviceVersion
                );
                RpcServiceContext rpcServiceContext = new RpcServiceContext();
                rpcServiceContext.object = serviceBean;
                Method[] methods = i.getMethods();
                for (Method m : methods) {
                    rpcServiceContext.methodMap.put(m.toString(), m);
                }
                float qps = qsRpcService.qps();
                if (qps > 0) {
                    rpcServiceContext.rateLimiter = RateLimiter.create(qps);
                    rpcServiceContext.qps = qps;
                }

                if (contextMap.containsKey(serviceBeanName)) {
                    throw new DuplicateException(serviceBeanName+" Has been registered ！！！");
                }

                contextMap.put(serviceBeanName, rpcServiceContext);
            }
        }
        init();
    }

    private void init() {
        if (contextMap.isEmpty()) {
            logger.info("没有发现@XLRpcService服务>_<");
            return;
        }

        /**初始化服务信息*/
        RpcServiceStarter.getInstance().init(contextMap, enableQSRpc);
        /**启动服务端*/
        RpcServiceStarter.getInstance().start();
        logger.info("XLRPC节点服务已启动^_^:" + contextMap.keySet());

        printServerInfo();
    }


    /**
     * 打印当前服务的信息
     */
    private void printServerInfo() {

        printScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, RpcServiceContext> stringRpcServiceContextEntry : contextMap.entrySet()) {
                    logger.info("@@@@@@ ServiceName :{} ServiceQps :{} Service RateLimit:{}", stringRpcServiceContextEntry.getKey(),
                            stringRpcServiceContextEntry.getValue().qps,
                            stringRpcServiceContextEntry.getValue().rateLimiter.getRate());
                }
            }
        }, 0, 60*1000, TimeUnit.MILLISECONDS);
    }


}
