package com.xl.rpc.client.manager;

import com.xl.rpc.message.Message;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public enum CallHelper {
    INSTANCE;
    private static final int MAX_RETRIES = 3;
    private static final ThreadPoolExecutor callPool = new ThreadPoolExecutor(128,
            256, 10L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    private final ConcurrentHashMap<Long, CompletableFuture<Message>> callFutureMap = new ConcurrentHashMap<>();


    public boolean response(Message rpcResponse) {
        long requestId = (long) rpcResponse.getId();
        CompletableFuture<Message> future = callFutureMap.get(requestId);
        if (future == null) {
//            log.error("request future is null, requestId={}", requestId);
            return false;
        }
        callFutureMap.remove(requestId);
//        log.info("receive reponse, requestId={}", requestId);
        future.complete(rpcResponse);
        return true;
    }

    public Message call(Channel channel, Message request, int timeout) {
        try {
            long start = System.currentTimeMillis();
            Message response = retryCallAsync(channel, request, timeout);
//            log.info("call request={}, cost={}", request, System.currentTimeMillis() - start);
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            callFutureMap.remove(request.getId());
        }
    }

    public Message retryCallAsync(Channel channel, Message request, int timeout) {
        int attempt = 0;
        long delay = 1500;
        while (attempt < MAX_RETRIES) {
            try {
//                RpcBuf.RpcRequest.Builder builder = request.toBuilder();
//                Map<String, String> extra = builder.getMutableExtra();
//                extra.put("attempt", String.valueOf(attempt));
                channel.writeAndFlush(request);
                CompletableFuture<Message> future = new CompletableFuture<>();
                callFutureMap.put((long) request.getId(), future);
                return future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
//                log.info("wait call response timed out, retrying... attempt={} requestId={}", attempt, request.getId());
                throw new RuntimeException("wait call response timed out, retrying... attempt=" + attempt + " requestId=" + request.getId());
            } catch (Exception e) {
                log.error("call requestId={} attempt={} error", request.getId(), attempt, e);
                throw new RuntimeException(e);
            }

//            attempt++;
//            if (attempt < 3) {
//                // 计算下次重试的延迟时间，并加入抖动
//                delay = Math.min(5000, delay * 2);
//                long jitter = ThreadLocalRandom.current().nextLong(delay / 2);
//                long waitTime = delay + jitter;
//
//                log.info("Retrying in waitTime={} ms...attempt={} requestId={}", waitTime, attempt, request.getId());
//                try {
//                    Thread.sleep(waitTime);
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                    throw new RuntimeException(e);
//                }
//            } else {
//                log.info("Max retries reached, giving up. attempt={} requestId={}", attempt, request.getId());
//                return request;
//            }
        }
        return request;
    }
}
