package com.xl.rpc.starter.common.exc;

/**
 * @author  by xl
 * @date 2020/12/09
 * @desc 服务重复注册
 */
public class DuplicateException extends Exception {

    public DuplicateException(String errMsg) {
        super("DuplicateException "+errMsg);
    }
}
