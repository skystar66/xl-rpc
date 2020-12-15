package com.xl.rpc.starter.common.exc;

/**
 * @author by xl
 * @date 2020/12/09
 * @desc 拒绝处理
 */
public class UnavailableException extends Exception {
    public UnavailableException() {
        super("UnavailableException");
    }
}
