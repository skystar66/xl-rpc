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


    /**
     * 接口名称
     */
    private String interfaceName;

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


    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
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
}
