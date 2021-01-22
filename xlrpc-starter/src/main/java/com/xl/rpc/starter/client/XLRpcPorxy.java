package com.xl.rpc.starter.client;

import com.alibaba.fastjson.JSONObject;
import com.xl.rpc.client.remote.RemoteRpcClientManager;
import com.xl.rpc.starter.common.serialize.ISerialize;
import com.xl.rpc.starter.common.utils.BeanNameUtil;
import com.xl.rpc.starter.common.utils.CGlib;
import com.xl.rpc.starter.dto.Request;
import com.xl.rpc.starter.dto.Response;
import com.xl.rpc.utils.SnowflakeIdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * Created by xl
 * Date 2020/11/13
 * 创建接口代理,实现远程调用
 */
public class XLRpcPorxy implements MethodInterceptor {



    private static Logger logger = LoggerFactory.getLogger(XLRpcPorxy.class);
    private Class target; // 代理对象接口

    private XLRpcReference XLRpcReference;
    private String serviceBeanName, version;
    private int timeout;
    private String action;//选择action

    private ISerialize iSerialize;


    public XLRpcPorxy(Class target, XLRpcReference XLRpcReference, ISerialize iSerialize) {
        this.target = target;
        this.XLRpcReference = XLRpcReference;
        this.iSerialize = iSerialize;
        this.version=XLRpcReference.version();

        this.serviceBeanName = BeanNameUtil.getInstance().getServiceBeanName(target.getName(),
                XLRpcReference.value(),XLRpcReference.version());
        this.action=serviceBeanName;

        timeout = XLRpcReference.timeout();
        if (timeout <= 0) timeout = RemoteRpcClientManager.RpcTimeout;
    }

    public Object getPorxy() {
        return CGlib.getPorxy(target, this);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

        long start = System.currentTimeMillis();
        Request request = new Request();
        request.setServiceBeanName(serviceBeanName);
        request.setVersion(version);
        request.setMethodName(method.toString());
        request.setParamters(objects);
        /**请求id*/
        Long reqId = SnowflakeIdWorker.getInstance().nextId();
        request.setReqId(reqId);
        logger.info("Client Invoke Request Server reqId: {}  | serviceBeanName: {} | Params: {}"
                ,reqId,serviceBeanName,request.getParamters());
        byte[] bytes = RemoteRpcClientManager.getInstance().sendSync(action, iSerialize.serialize(request));
        Response response = iSerialize.deserialize(bytes, Response.class);
        logger.info("Client Invoke Recive Server Response Cost Time: {}ms | reqId: {} | serviceBeanName: {} | Response: {}"
                ,System.currentTimeMillis()-start,reqId,serviceBeanName,JSONObject.toJSONString(response
                ));
        if (response.getException() != null) {
            throw response.getException();
        }
        return response.getResult();
    }


}

