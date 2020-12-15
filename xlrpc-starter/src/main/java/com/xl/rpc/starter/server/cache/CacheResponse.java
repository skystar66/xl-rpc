package com.xl.rpc.starter.server.cache;

import com.xl.rpc.starter.common.exc.NotFoundException;
import com.xl.rpc.starter.common.exc.UnavailableException;
import com.xl.rpc.starter.common.serialize.ISerialize;
import com.xl.rpc.starter.dto.Response;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author  by xl
 * @date 2020/12/09
 * @desc 缓存通用响应参数
 */
public class CacheResponse {

    private ISerialize iSerialize;
    private Map<String, byte[]> map = new ConcurrentHashMap<>();

    public CacheResponse(ISerialize iSerialize) {
        this.iSerialize = iSerialize;
        init();
    }


    private void init() {
        Response nofound = new Response();
        nofound.setException(new NotFoundException());
        map.put("nofound", iSerialize.serialize(nofound));

        Response unavailable = new Response();
        unavailable.setException(new UnavailableException());
        map.put("unavailable", iSerialize.serialize(unavailable));

        Response empty = new Response();
        map.put("empty", iSerialize.serialize(empty));
    }

    public byte[] nofound() {
        return map.get("nofound");
    }

    public byte[] unavailable() {
        return map.get("unavailable");
    }

    public byte[] empty() {
        return map.get("empty");
    }

}
