package com.xl.rpc.mq;

import java.time.Duration;

public interface MessageQueue<T> {

    boolean push(T msg, Duration maxWait);

    T pop(Duration maxWait);

    int size();
}
