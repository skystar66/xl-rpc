package com.xl.rpc.starter.common.serialize;

/**
 * Created by xl
 * Date 2020/12/20
 */
public interface ISerialize {


    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] data, Class<T> cls);
}
