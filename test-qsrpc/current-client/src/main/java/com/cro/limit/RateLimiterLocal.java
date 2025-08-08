package com.cro.limit;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RateLimiterLocal {
    // 每秒产生的令牌数
    private final long tokensPerSecond;
    private final long maxTokens;

    // 当前桶中令牌数
    private AtomicLong availableTokens;

    // 上次获取令牌的时间
    private AtomicLong lastRefillTime;

    // 锁对象，保证并发安全
    private final Object lock = new Object();

    public RateLimiterLocal(long tokensPerSecond, long maxTokens) {
        this.tokensPerSecond = tokensPerSecond;
        this.maxTokens = maxTokens;
        this.availableTokens = new AtomicLong(maxTokens);
        this.lastRefillTime = new AtomicLong(System.nanoTime());
    }

    // 获取令牌，返回是否成功
    public boolean tryAcquire(){
        long currentTime = System.nanoTime();

        synchronized (lock) {
            // 计算时间差（纳秒）
            long elapsedTime = currentTime - lastRefillTime.get();
            long tokensToAdd = elapsedTime * tokensPerSecond / TimeUnit.SECONDS.toNanos(1);

            // 限制令牌数不能超过最大值
            if (tokensToAdd > 0) {
                availableTokens.set(Math.min(maxTokens, availableTokens.get() + tokensToAdd));
                lastRefillTime.set(currentTime);
            }

            // 如果没有令牌可用，返回 false
            if (availableTokens.get() > 0) {
                availableTokens.decrementAndGet();
                return true;
            } else {
                return false;
            }
        }
    }
}
