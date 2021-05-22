package com.cro.reset;


import com.xl.rpc.callback.Callback;
import com.xl.rpc.client.RpcClient;
import com.xl.rpc.client.pool.NodePoolManager;
import com.xl.rpc.exception.RPCException;
import com.xl.rpc.message.Message;
import com.xl.rpc.message.MessageBuf;
import com.xl.rpc.zk.NodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@RestController
@RequestMapping("current")
@Slf4j
public class CurrentController {

    private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE,
            DEFAULT_THREAD_POOL_SIZE * 2, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024));


    private static ScheduledExecutorService schedule = Executors.newScheduledThreadPool(10);

    private final static int PORT = 10086;
    private final static int count = 41667;// 8 * 125000=100万请求
//        private final static int count = 1;//
    private final static int thread = DEFAULT_THREAD_POOL_SIZE;//x个请求线程
    private final static long totalReqCount = count * thread;//总共请求
//    private final static long totalReqCount = 1000000;//总共请求
    private final static String zip = "";//gzip snappy
    private final static int timeout = 60_000;


    //加上包头包尾长度12字节,可加大测试带宽
    private static byte[] req = new byte[116];


    static {
//        info.setZip(zip);
        /**初始化客户端连接池*/
//        NodePoolManager.getInstance().initNodePool("com.qrpc.api.ApiServerapiServer");
    }


    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public String init() {
        /**初始化客户端连接池*/
        NodePoolManager.getInstance().initNodePool("com.qrpc.api.ApiServerapiServer");
        return "success";

    }

    /**
     * 异步压测
     * @desc:
     *       1,160万并发 压测结果：4core：time:13403ms ,qps:119376个 ,流量:14922KB/s ,平均请求延时:8194ms
     *       2,100万并发 压测结果：4-core-> time:7843ms ,qps:127502个 ,流量:15937KB/s ,平均请求延时:3922ms
     *       3，100万并发 压测结果：12-core-> 1000008请求 -> time:20479ms ,qps:48830/s ,流量:7296KB/s ,平均请求延时:0ms
     */
    @RequestMapping(value = "/clientAsync", method = RequestMethod.GET)
    public String clientAsync() {
        //todo
        for (int i = 0; i < thread; i++) {
            //160万并发：4-core-> time:13403ms ,qps:119376个 ,流量:14922KB/s ,平均请求延时:8194ms
            //100万并发  4-core-> time:7843ms ,qps:127502个 ,流量:15937KB/s ,平均请求延时:3922ms
            EXECUTOR_SERVICE.submit(asyncPOOL);
        }
//        schedule.scheduleAtFixedRate(asyncPOOL,
//                0,500, TimeUnit.MILLISECONDS);
        temp = System.currentTimeMillis();
        return "success";
    }

    /**
     * 同步压测
     * @desc:
     *       1，160万并发 压测结果：4-core-> use time:116627ms ,qps:13718个 ,流量:1714KB/s ,平均请求延时:0ms
     *       2，100万并发 压测结果：4-core-> use time:75253ms ,qps:13288个 ,流量:1661KB/s ,平均请求延时:0ms
     *       3，100万并发 压测结果：12-core-> 1000008请求 -> time:20049ms ,qps:49878/s ,流量:7452KB/s ,平均请求延时:0ms
     */
    @RequestMapping(value = "/clientSync", method = RequestMethod.GET)
    public String client() {
        for (int i = 0; i < thread; i++) {
            //160万并发：4-core-> use time:116627ms ,qps:13718个 ,流量:1714KB/s ,平均请求延时:0ms
            //100万并发：4-core-> use time:75253ms ,qps:13288个 ,流量:1661KB/s ,平均请求延时:0ms
            EXECUTOR_SERVICE.submit(syncPOOL);
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
            String content = System.currentTimeMillis()+"dhasiuhdijdoiasjdoiwoiraosdhauifhi";

            for (int i = 0; i < count; i++) {
                Message msg = new Message();
                msg.setContent(makeMessage().toByteArray());
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
                log.error(Runtime.getRuntime().availableProcessors()
                        + "-core-> "+totalReqCount+"请求 -> time:" + use + "ms" +
                        " ,qps:" + totalReqCount * 1000 / use + "/s" +
                        " ,流量:" + totalReqCount * (res.getContent().length + 12) / 1024 * 1000 / use + "KB/s" +
                        " ,平均请求延时:" + (use / totalReqCount) + "ms"
                );
            }
        }

        @Override
        public void handleError(Throwable error) {
            error.printStackTrace();
            System.out.println("handleError-" + error);
        }
    };


    public static MessageBuf.IMMessage makeMessage(){
        MessageBuf.IMMessage.Builder msgBuilder = MessageBuf.IMMessage.newBuilder();
        msgBuilder.setFrom(UUID.randomUUID().toString());
        msgBuilder.setTo("0098778899");
        msgBuilder.setContent("12321321312sddasdas"+System.currentTimeMillis());
        msgBuilder.setCMsgId(System.currentTimeMillis());
        msgBuilder.setType(MessageBuf.TypeEnum.ROOM_VALUE);
        msgBuilder.setSubType(MessageBuf.SubTypeEnum.ROOM_DIY_VALUE);
        msgBuilder.setDeviceId(UUID.randomUUID().toString());
        msgBuilder.setAppId("liveme");
        return msgBuilder.build();
    }

    //同步POOL
    static Runnable syncPOOL = new Runnable() {

        @Override
        public void run() {
            for (int i = 0; i < count; i++) {
                Message msg = new Message();
                msg.setContent(makeMessage().toByteArray());
                Message res = sendSyncTest(msg);
                requse += (System.currentTimeMillis() - map.get(res.getId()));

                if (res.getId() == totalReqCount) {
                    System.out.println("callback id-" + res.getId());
                    long use = System.currentTimeMillis() - temp;
                    log.error(totalReqCount+"请求 -> use time:" + use + "ms" +
                            " ,qps:" + totalReqCount * 1000 / use + "/s" +
                            " ,流量:" + totalReqCount * (req.length + 12) * 1000 / use / 1024 + "KB/s" +
                            " ,平均请求延时:" + (use / totalReqCount) + "ms");
                }
            }
        }
    };


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
        RpcClient tcpClient = NodePoolManager.getInstance().
                chooseRpcClient("com.qrpc.api.ApiServerapiServer");
        if (tcpClient != null) {
            request.setId(Message.createID());
            map.put(request.getId(), System.currentTimeMillis());
            tcpClient.sendAsync(request, callback, timeout);
        }
    }

}
