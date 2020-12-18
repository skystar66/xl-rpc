package com.xl.rpc.context;


import com.xl.rpc.enums.NettyType;

/**
 * Description: 用于标识 服务类型
 * Date: 2020/12/21
 *
 * @author xulia
 */
public class NettyContext {

    public static NettyType nettyType;


    public static NettyType currentType() {
        return nettyType;
    }

    public static Object params;

    public static <T> T currentParam(Class<T> tClass) {
        return (T) params;
    }


}
