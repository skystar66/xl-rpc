package com.qrpc.client.controller;


import com.xl.rpc.callback.Callback;
import com.xl.rpc.client.RpcClient;
import com.xl.rpc.client.pool.NodePoolManager;
import com.xl.rpc.config.ServerConfig;
import com.xl.rpc.exception.RPCException;
import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.log.Log;
import com.xl.rpc.message.Message;
import com.xl.rpc.register.NodeBuilder;
import com.xl.rpc.server.ServerStarter;
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

    private static String[] actions=new String[]{"com.qrpc.api.ApiServerapiServer"};


    //加上包头包尾长度12字节,可加大测试带宽
    private static byte[] req = new byte[116];


    static {
//        info.setZip(zip);
        /**初始化客户端连接池*/
//        NodePoolManager.getInstance().initNodePool();

    }


    @RequestMapping(value = "/server",method = RequestMethod.GET)
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

        }catch (Exception ex) {

        }
        List<String> nodeDatas = ZkHelp.getInstance().getChildren(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH));

        return nodeDatas.toString();

    }



    @RequestMapping(value = "/init",method = RequestMethod.GET)
    public String init() {
        /**初始化客户端连接池*/
        NodePoolManager.getInstance().initNodePool();
        return "success";

    }




    @RequestMapping(value = "/client",method = RequestMethod.GET)
    public String client() {

        for (int i = 0; i < thread; i++) {

            //异步线程池
            EXECUTOR_SERVICE.submit(asyncPOOL);

            //同步线程池,本无业务逻辑测试qps只有异步的30% ,猜测请求线程频繁休眠唤醒耗费性能
//            EXECUTOR_SERVICE.submit(syncPOOL);
        }
        temp = System.currentTimeMillis();

        return "success";

    }


    //=================使用连接池=================
    static long temp = 0;
    static volatile long requse;
    static volatile Map<Integer, Long> map = new ConcurrentHashMap<>();

    private static synchronized void requestAdd(int id) {
        requse += (System.currentTimeMillis() - map.get(id));
    }

    //异步POOL
    //4-core-> time:7774 ,qps:128633 ,流量:16079KB/s ,平均请求延时:360
    static Runnable asyncPOOL = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                Message msg = new Message();
                msg.setContent(req);
                sendAsyncTest(msg, callback);
            }
        }
    };
    //异步POOL回调
    static Callback<Message> callback = new Callback<Message>() {

        @Override
        public void handleResult(Message res) {
            requestAdd(res.getId());
            if (res.getId() == totalReqCount) {
                System.out.println("callback id-" + res.getId());
                long use = System.currentTimeMillis() - temp;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.err.println(Runtime.getRuntime().availableProcessors() + "-core-> time:" + use +
                        " ,qps:" + totalReqCount * 1000 / use +
                        " ,流量:" + totalReqCount * (res.getContent().length + 12) / 1024 * 1000 / use + "KB/s" +
                        " ,平均请求延时:" + (requse / totalReqCount)
                );
            }
        }

        @Override
        public void handleError(Throwable error) {
            error.printStackTrace();
            System.out.println("handleError-" + error);
        }
    };


    //同步POOL
    static Runnable syncPOOL = new Runnable() {

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                Message msg = new Message();
                msg.setContent(req);
                Message res = sendSyncTest(msg);
                requse += (System.currentTimeMillis() - map.get(res.getId()));

                if (res.getId() == totalReqCount) {
                    System.out.println("callback id-" + res.getId());
                    long use = System.currentTimeMillis() - temp;
                    System.err.println("use time:" + use +
                            " ,qps:" + totalReqCount * 1000 / use +
                            " ,流量:" + totalReqCount * (req.length + 12) * 1000 / use / 1024 + "KB/s" +
                            " ,平均请求延时:" + (requse / totalReqCount));
                }
            }
        }
    };

    // ==================建立一个 pool==================
    static NodeInfo info = new NodeInfo();

    //同步
    static Message sendSyncTest(Message request) {

        RpcClient tcpClient = NodePoolManager.getInstance().chooseRpcClient("com.qrpc.api.ApiServerapiServer");
        if (tcpClient != null) {
            try {
                request.setId(Message.createID());
                map.put(request.getId(), System.currentTimeMillis());
                return tcpClient.sendSync(request, timeout);
            } catch (RPCException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //异步
    static void sendAsyncTest(Message request, Callback<Message> callback) {
        RpcClient tcpClient =NodePoolManager.getInstance().chooseRpcClient("com.qrpc.api.ApiServerapiServer");
        if (tcpClient != null) {
            request.setId(Message.createID());
            map.put(request.getId(), System.currentTimeMillis());
            tcpClient.sendAsync(request, callback, timeout);
        }
    }

}
