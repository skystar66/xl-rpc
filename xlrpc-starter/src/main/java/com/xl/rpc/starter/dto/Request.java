package com.xl.rpc.starter.dto;

import java.io.Serializable;


/**
 * Created by xl
 * <p>
 * Date 2020/12/9
 *
 * @desc 请求参数
 */
public class Request implements Serializable {



    /**请求唯一id*/
    private Long reqId;

    /**
     * 接口名称
     */
    private String serviceBeanName;

    /**
     * 版本号
     */
    private String version;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数paramters
     */
    private Object[] paramters;


    public String getServiceBeanName() {
        return serviceBeanName;
    }

    public void setServiceBeanName(String serviceBeanName) {
        this.serviceBeanName = serviceBeanName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParamters() {
        return paramters;
    }

    public void setParamters(Object[] paramters) {
        this.paramters = paramters;
    }

    public Long getReqId() {
        return reqId;
    }

    public void setReqId(Long reqId) {
        this.reqId = reqId;
    }
}
