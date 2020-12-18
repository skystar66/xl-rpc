package com.xl.rpc.starter.client;

import com.xl.rpc.client.remote.RemoteRpcClientManager;
import com.xl.rpc.starter.common.serialize.ISerialize;
import com.xl.rpc.starter.common.utils.CGlib;
import com.xl.rpc.starter.dto.Request;
import com.xl.rpc.starter.dto.Response;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by xl
 * Date 2020/11/13
 * 创建接口代理,实现远程调用
 */
public class XLRpcPorxy implements MethodInterceptor {

    private Class target; // 代理对象接口

    private XLRpcReference XLRpcReference;
    private String interfaceName, version;
    private int timeout;
    private String action;//选择action

    private ISerialize iSerialize;


    public XLRpcPorxy(Class target, XLRpcReference XLRpcReference, ISerialize iSerialize) {
        this.target = target;
        this.XLRpcReference = XLRpcReference;
        this.iSerialize = iSerialize;
        //初始化数据
        interfaceName = target.getName();
        if (XLRpcReference.value().isEmpty()) {
            version = XLRpcReference.version();
        } else {
            version = XLRpcReference.value();
        }
        if (XLRpcReference.ip_port().isEmpty()) {
            action = interfaceName + XLRpcReference.value();
        } else {
            action = XLRpcReference.ip_port();
        }

        timeout = XLRpcReference.timeout();
        if (timeout <= 0) timeout = RemoteRpcClientManager.RpcTimeout;
    }

    public Object getPorxy() {
        return CGlib.getPorxy(target, this);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        Request request = new Request();
        request.setInterfaceName(interfaceName);
        request.setVersion(version);
        request.setMethodName(method.toString());
        request.setParamters(objects);

        byte[] bytes = RemoteRpcClientManager.getInstance().sendSync(action, iSerialize.serialize(request));
        Response response = iSerialize.deserialize(bytes, Response.class);
        if (response.getException() != null) {
            throw response.getException();
        }
        return response.getResult();
    }


}

