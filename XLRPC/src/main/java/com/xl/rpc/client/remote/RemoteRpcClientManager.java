package com.xl.rpc.client.remote;

import com.xl.rpc.callback.CallFuture;
import com.xl.rpc.callback.Callback;
import com.xl.rpc.client.RpcClient;
import com.xl.rpc.client.manager.RpcClientManager;
import com.xl.rpc.client.pool.NodePoolManager;
import com.xl.rpc.config.ServerConfig;
import com.xl.rpc.exception.RPCException;
import com.xl.rpc.message.Message;
import com.xl.rpc.statistics.StatisticsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 远程rpc 调用
 */
public class RemoteRpcClientManager {


    /**请求rpc超时时间，默认是60s*/
    public static final int RpcTimeout;

    static {
        if (ServerConfig.containsKey(ServerConfig.KEY_RPC_CONNECT_TIMEOUT)) {
            RpcTimeout = ServerConfig.getInt(ServerConfig.KEY_RPC_CONNECT_TIMEOUT);
        } else {
            RpcTimeout = 60 * 1000;
        }
    }

    private static Logger logger = LoggerFactory.getLogger(RemoteRpcClientManager.class);

    private static class InstanceHolder {
        public static final RemoteRpcClientManager instance = new RemoteRpcClientManager();
    }

    public static RemoteRpcClientManager getInstance() {
        return RemoteRpcClientManager.InstanceHolder.instance;
    }


    /**
     * 同步发送,阻塞,
     * <p>
     * 访问延迟大将会导致线程挂起太久,CPU无法跑满,而解决方法只有新建更多线程,性能不好,
     * <p>
     * 如作为主请求路由RPC强烈不建议用
     *
     * @throws InterruptedException
     * @throws RPCException
     */
    public byte[] sendSync(String action, byte[] content) throws RPCException, InterruptedException {
        return sendAsync(action, content, RpcTimeout)
                .get(RpcTimeout, TimeUnit.MILLISECONDS);
    }


    /**
     * 异步Future
     */
    public CallFuture<byte[]> sendAsync(String action, byte[] content, int timeout) {
        CallFuture<byte[]> callFuture = CallFuture.newInstance();
        sendAsync(action, content, callFuture, timeout);
        return callFuture;
    }


    /**
     * 异步回调,nio
     */
    public boolean sendAsync(String action, byte[] content, Callback<byte[]> callback) {
        return sendAsync(action, content, callback, RpcTimeout);
    }


    /**
     * 异步回调,nio
     */

    public boolean sendAsync(String action, byte[] content, Callback<byte[]> callback, int rpcTimeOut) {

        try {
            //选择rpcClient
            RpcClient rpcClient = NodePoolManager.getInstance().chooseRpcClient(action);
            if (rpcClient != null) {
                Message request = new Message();
                request.setId(Message.createID());
                request.setContent(content);
                rpcClient.sendAsync(request, new AsyncCallback(callback, rpcClient.getIpPort(), request), rpcTimeOut);
                return true;
            } else {
                logger.error("can no choose pool:" + action);
                callback.handleError(new RPCException("can no choose pool:" + action));
                return false;
            }
        } catch (Exception ex) {
            callback.handleError(new RPCException("sendAsync error:", ex));

        }
        return false;
    }


    /**
     * 包装异步回调,统计服务端调用信息，做中介者模式
     */
    public static class AsyncCallback implements Callback<Message> {
        /**
         * 客户端自己的结果回调类
         */
        private Callback<byte[]> resultCallBack;
        private String ipport;

        public AsyncCallback(Callback<byte[]> resultCallBack, String ipport, Message message) {
            this.resultCallBack = resultCallBack;
            this.ipport = ipport;
            /**统计*/
            StatisticsManager.getInstance().start(ipport, message);

        }

        @Override
        public void handleResult(Message result) {
            resultCallBack.handleResult(result.getContent());
            /**统计*/
            StatisticsManager.getInstance().success(ipport, result);
        }

        @Override
        public void handleError(Throwable error) {
            resultCallBack.handleError(error);
            /**统计*/
            StatisticsManager.getInstance().fail(ipport, error);
        }
    }
}
