package com.xl.rpc.starter.common.exc;

/**
 * @author  by xl
 * @date 2020/12/09
 * @desc 未发现服务接口
 */
public class NotFoundException extends Exception {

    public NotFoundException() {
        super("NotFoundException");
    }
}
