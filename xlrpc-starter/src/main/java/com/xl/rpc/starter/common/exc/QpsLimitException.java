package com.xl.rpc.starter.common.exc;

/**
 * @author by xl
 * @date 2020/12/09
 * @desc 拒绝处理
 */
public class QpsLimitException extends Exception {
    public QpsLimitException() {
        super("QpsLimitException access limit！！！");
    }
}
