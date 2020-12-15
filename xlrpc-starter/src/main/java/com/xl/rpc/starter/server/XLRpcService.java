package com.xl.rpc.starter.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC 服务注解（标注在服务实现类上）
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface XLRpcService {


    /**
     * 服务版本号
     */
    String version() default "";


    /**
     * 服务名称
     */
    String value() default "";


    /**
     * 服务承载的qps
     */
    float qps() default -1;


}
