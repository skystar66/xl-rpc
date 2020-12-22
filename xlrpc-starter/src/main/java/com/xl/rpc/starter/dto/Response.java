package com.xl.rpc.starter.dto;

import java.io.Serializable;



/**
 * @author by xl
 * @date 2020/12/09
 * @desc 响应参数
 */
public class Response implements Serializable {

    private Exception exception;

    private Long reqId;

    private Object result;

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Long getReqId() {
        return reqId;
    }

    public void setReqId(Long reqId) {
        this.reqId = reqId;
    }
}
