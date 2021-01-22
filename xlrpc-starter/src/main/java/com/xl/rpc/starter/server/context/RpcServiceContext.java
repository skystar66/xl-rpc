package com.xl.rpc.starter.server.context;


import com.google.common.util.concurrent.RateLimiter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author by xl
 * @date 2020/12/09
 * @desc 存储一个rpc接口服务的信息
 */
public class RpcServiceContext {

    /**
     * 服务实例
     */
    public Object object;

    /**
     * 该服务所有的方法
     */
    public Map<String, Method> methodMap = new HashMap<>();

    /**该服务的qps大小*/
    public float qps;

    /**
     * 服务的限流qps
     */
    public RateLimiter rateLimiter;

}
