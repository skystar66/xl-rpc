package com.xl.rpc.starter.client;

import com.xl.rpc.client.monitor.ConnectQueueMonitor;
import com.xl.rpc.client.pool.NodePoolManager;
import com.xl.rpc.cluster.ClusterCenter;
import com.xl.rpc.starter.common.serialize.ISerialize;
import com.xl.rpc.starter.common.serialize.Protostuff;
import com.xl.rpc.starter.common.utils.AopTargetUtils;
import com.xl.rpc.starter.enable.EnableQSRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Map;


@Service
public class ClientListener implements ApplicationListener<ContextRefreshedEvent>, BeanPostProcessor {


    private static final Logger LOGGER = LoggerFactory.getLogger(ClientListener.class);


    @Override//装载完所有bean调用,此时还没启动http服务,ApplicationRunner是启动完http端口后的
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            initClient(event.getApplicationContext());
        }
    }


    private void initClient(ApplicationContext ctx) {
        Map<String, Object> startMap = ctx.getBeansWithAnnotation(EnableQSRpc.class);
        if (startMap.size() == 0) return;
        EnableQSRpc enableQSRpc = startMap.values().iterator().next().getClass().getAnnotation(EnableQSRpc.class);
        if (!enableQSRpc.enabledClient()) return;
        /**初始化连接池*/
        NodePoolManager.getInstance().initNodePool();
        /**监听节点变化*/
        ClusterCenter.getInstance().listenerServerRpc();
        /**监控连接池队列*/
        ConnectQueueMonitor.getInstance().start();
    }


    private ISerialize iSerialize;

    //扫描容器所有@QSRpcReference的变量,new一个代理赋值
    @Override//所有bean初始化完成前会调用
    public Object postProcessBeforeInitialization(final Object row, final String beanName) throws BeansException {
        if (iSerialize == null) iSerialize = new Protostuff();

        Object bean = row;
        try {
            bean = AopTargetUtils.getTarget(row);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException("获取被代理对象出错:" + row);
        }

        Class beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field f : fields) {
            XLRpcReference XLRpcReference = f.getAnnotation(XLRpcReference.class);
            if (XLRpcReference != null) {

                Class<?> c = f.getType();
                if (!c.isInterface()) new IllegalArgumentException(f + " @QSRpcReference 必须注解在一个接口上");
                Object porxy = new XLRpcPorxy(c, XLRpcReference, iSerialize).getPorxy();//创建代理

                f.setAccessible(true);
                try {
                    f.set(bean, porxy);
                    LOGGER.info("Create QSRpcPorxy: " + c.getName() + "--->" + bean);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return row;
    }
}



