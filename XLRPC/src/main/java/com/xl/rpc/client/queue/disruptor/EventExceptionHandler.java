package com.xl.rpc.client.queue.disruptor;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Date 2021/1/14 17:39
 * @Author wanghao2@rongcloud.cn
 */
@Slf4j
public class EventExceptionHandler implements ExceptionHandler {
    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        log.error("sequence is {},event is {}", sequence, event);
        log.error("", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        log.error("", ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        log.error("", ex);
    }
}
