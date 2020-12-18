package com.xl.rpc.test;

import com.xl.rpc.config.ServerConfig;
import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.log.Log;
import com.xl.rpc.server.ServerStarter;
import com.xl.rpc.server.node.NodeBuilder;
import com.xl.rpc.utils.AddressUtils;
import com.xl.rpc.zk.NodeInfo;
import com.xl.rpc.zookeeper.ZkHelp;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class server {

    private static final int DEFAULT_THREAD_POOL_SIZE = 8;//Runtime.getRuntime().availableProcessors() * 2;

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE,
            DEFAULT_THREAD_POOL_SIZE * 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024));

    private final static int PORT = 10086;
    //    private final static int count = 125000;//
    private final static int count = 1;//
    private final static int thread = DEFAULT_THREAD_POOL_SIZE;//x个请求线程
    private final static long totalReqCount = count * thread;//总共请求
    private final static String zip = "";//gzip snappy
    private final static int timeout = 60_000;

    private static String[] actions=new String[]{"test"};


    //加上包头包尾长度12字节,可加大测试带宽
    private static byte[] req = new byte[116];


    public static void main(String[] args) {

        NodeInfo info = NodeBuilder.buildNode();
        info.setActions(actions);
        info.setRpcPoolSize(3);
        info.setIp(AddressUtils.getInnetIp());
        info.setPort(PORT);
        info.setZkRpcPath("/xlrpc");
        info.setActions(actions);

        /**开启服务端*/
        new Thread(new ServerStarter(info, new MessageListener() {
            @Override
            public byte[] onMessage(byte[] message) {
                return message;
            }
        })).start();

        Log.i("server start ok!");
        try {
            Thread.sleep(2000);

        }catch (Exception ex) {

        }
        List<String> nodeDatas = ZkHelp.getInstance().getChildren(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH));
        System.out.println(nodeDatas.toString());
    }


}
