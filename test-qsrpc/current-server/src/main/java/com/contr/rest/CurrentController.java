package com.contr.rest;


import com.xl.rpc.callback.Callback;
import com.xl.rpc.client.RpcClient;
import com.xl.rpc.client.pool.NodePoolManager;
import com.xl.rpc.config.ServerConfig;
import com.xl.rpc.exception.RPCException;
import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.log.Log;
import com.xl.rpc.message.Message;
import com.xl.rpc.server.ServerStarter;
import com.xl.rpc.server.node.NodeBuilder;
import com.xl.rpc.zk.NodeInfo;
import com.xl.rpc.zookeeper.ZkHelp;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@RestController
@RequestMapping("current")
public class CurrentController {

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

    private static String[] actions = new String[]{"com.qrpc.api.ApiServerapiServer"};


    //加上包头包尾长度12字节,可加大测试带宽
    private static byte[] req = new byte[116];


    static {
//        info.setZip(zip);
        /**初始化客户端连接池*/
//        NodePoolManager.getInstance().initNodePool();

    }


    /**
     * 模拟开启一个服务端 注册zookeeper
     */
    @RequestMapping(value = "/server", method = RequestMethod.GET)
    public String hello() {

        NodeInfo info = NodeBuilder.buildNode();
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

        } catch (Exception ex) {

        }
        List<String> nodeDatas = ZkHelp.getInstance().getChildren(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH));

        return ZkHelp.getInstance().getValue("/xlrpc/" + nodeDatas.get(0));

    }

    /**
     * 模拟开启一个服务端 无需注册zookeeper，本地开启
     */
    @RequestMapping(value = "/server2", method = RequestMethod.GET)
    public String server2() {

        NodeInfo info = NodeBuilder.buildNode();
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

        } catch (Exception ex) {

        }

        return "success";

    }


}
