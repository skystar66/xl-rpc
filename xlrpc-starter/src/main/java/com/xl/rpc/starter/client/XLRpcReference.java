package com.xl.rpc.starter.client;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Created by xl
 * @Date 2020/11/12
 * @desc 客户端引用注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface XLRpcReference {

    /**
     * 服务名称
     */
    String value() default "";

    /**
     * 服务版本号
     */
    String version() default "";


    /**请求服务的超时时间*/
    int timeout() default -1;//默认60s


    /**指定请求服务的ipport*/
    String ip_port() default "";//指定要请求的服务端127.0.0.1:8080

}
