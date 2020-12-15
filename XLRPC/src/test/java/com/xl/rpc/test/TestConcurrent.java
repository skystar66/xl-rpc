package com.xl.rpc.test;


import com.xl.rpc.callback.Callback;
import com.xl.rpc.client.RpcClient;
import com.xl.rpc.client.pool.NodePoolManager;
import com.xl.rpc.exception.RPCException;
import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.log.Log;
import com.xl.rpc.message.Message;
import com.xl.rpc.register.NodeBuilder;
import com.xl.rpc.server.ServerStarter;
import com.xl.rpc.zk.NodeInfo;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * @author xl
 * @date 2020年12月26日 下午7:59:14
 * <p>
 * 类说明
 * tcp链接性能测试,发送和接收都是本程序,所以测试结果低于理论性能
 * cpu:8100
 * TODO 测试需要关闭run日志窗口,360等杀毒软件,非常影响测试性能
 */
public class TestConcurrent {


    static {

        System.setProperty("config.type", "/netty/dev");
        System.setProperty("xlrpc.zk.ips", "127.0.0.1:2181");
        System.setProperty("xlrpc.node.ip", "127.0.0.1");
        System.setProperty("xlrpc.node.port", "19980");
        System.setProperty("xlrpc.zk.path", "/xlrpc");
        System.setProperty("xlrpc.node.weight", "20");
        System.setProperty("xlrpc.node.zip", "snappy/gzip");
        System.setProperty("xlrpc.node.rpc.size", "3");



    }
    private static final int DEFAULT_THREAD_POOL_SIZE = 8;//Runtime.getRuntime().availableProcessors() * 2;

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE,
            DEFAULT_THREAD_POOL_SIZE * 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024));

    private final static int PORT = 10086;
//    private final static int count = 125000;//
    private final static int count = 1;//
    private final static int thread = DEFAULT_THREAD_POOL_SIZE;//x个请求线程
    private final static long len = count * thread;//总共请求
    private final static String zip = "";//gzip snappy
    private final static int timeout = 60_000;

    private static String[] actions=new String[]{"test"};


    //加上包头包尾长度12字节,可加大测试带宽
    private static byte[] req = new byte[116];


    public static void main(String[] args) throws IOException {
        NodeInfo info = new NodeInfo();
        info.setRpcPoolSize(3);
        info.setPort(PORT);
        info.setActions(actions);
        info.setZkRpcPath("/xlrpc");

        /**开启服务端*/
        new Thread(new ServerStarter(info, new MessageListener() {
            @Override
            public byte[] onMessage(byte[] message) {
                return message;
            }
        })).start();


        Log.i("server start ok!");

        for (int i = 0; i < thread; i++) {

            //异步线程池
//            EXECUTOR_SERVICE.submit(asyncPOOL);

            //同步线程池,本无业务逻辑测试qps只有异步的30% ,猜测请求线程频繁休眠唤醒耗费性能
//            EXECUTOR_SERVICE.submit(syncPOOL);
        }
        temp = System.currentTimeMillis();

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
            if (res.getId() == len) {
                System.out.println("callback id-" + res.getId());
                long use = System.currentTimeMillis() - temp;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.err.println(Runtime.getRuntime().availableProcessors() + "-core-> time:" + use +
                        " ,qps:" + len * 1000 / use +
                        " ,流量:" + len * (res.getContent().length + 12) / 1024 * 1000 / use + "KB/s" +
                        " ,平均请求延时:" + (requse / len)
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

                if (res.getId() == len) {
                    System.out.println("callback id-" + res.getId());
                    long use = System.currentTimeMillis() - temp;
                    System.err.println("use time:" + use +
                            " ,qps:" + len * 1000 / use +
                            " ,流量:" + len * (req.length + 12) * 1000 / use / 1024 + "KB/s" +
                            " ,平均请求延时:" + (requse / len));
                }
            }
        }
    };

    // ==================建立一个 pool==================
    static NodeInfo info = new NodeInfo();

    static {
        info.setZip(zip);
        /**初始化客户端连接池*/
//        NodePoolManager.getInstance().initNodePool();

    }


    //同步
    static Message sendSyncTest(Message request) {

        RpcClient tcpClient =NodePoolManager.getInstance().chooseRpcClient("test");
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
        RpcClient tcpClient =NodePoolManager.getInstance().chooseRpcClient("test");
        if (tcpClient != null) {
            request.setId(Message.createID());
            map.put(request.getId(), System.currentTimeMillis());
            tcpClient.sendAsync(request, callback, timeout);
        }
    }
}
