package com.xl.rpc.starter.enable;

import com.xl.rpc.starter.client.ClientListener;
import com.xl.rpc.starter.common.serialize.Protostuff;
import com.xl.rpc.starter.server.listener.ServiceListener;
import com.xl.rpc.starter.server.starter.RpcServiceStarter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 用在SpringBoot项目的启动类上的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ServiceListener.class
        , ClientListener.class
        , Protostuff.class})
public @interface EnableXLRpc {


    /**
     * 是否启动rpc
     */
    boolean enabledServer() default false;
    boolean enabledClient() default false;

    /**
     * 全局qps,针对本服务所有rpc接口请求之和
     */
    float qps() default -1;
}
