package com.xl.rpc.client.loadbalance.weight;

import com.xl.rpc.client.connect.NodePoolCache;
import com.xl.rpc.client.remote.RemoteRpcClientManager;
import com.xl.rpc.exception.RPCException;
import com.xl.rpc.zk.NodeInfo;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 负载均衡
 *
 * @desc 服务权重算法类
 */
public class RpcLoadBalance {

    private static Logger logger = LoggerFactory.getLogger(RpcLoadBalance.class);

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);


    private static class InstanceHolder {
        public static final RpcLoadBalance instance = new RpcLoadBalance();
    }

    public static RpcLoadBalance getInstance() {
        return RpcLoadBalance.InstanceHolder.instance;
    }

    static {
        statisPrintLoadBalanceSrv();
    }

    private static List<NodeInfo> nodeInfos = new CopyOnWriteArrayList<>();

    private short[] indexMap;//下标为weight,值为nodeInfos对应的节点index,提升选择性能
    private volatile int weightIndex = -1;
    private int weightSum;
    private Random random = new Random();

    public void addNode(NodeInfo nodeInfo) {
        if (!nodeInfos.contains(nodeInfo)) {
            nodeInfos.add(nodeInfo);
        }
    }

    /**
     * 移除掉对应ip
     */
    public void removeNode(String node) {
        nodeInfos = nodeInfos.stream()
                .filter(ip -> ip.equals(node))
                .collect(Collectors.toList());
    }

    /**
     * 根据权重获取节点
     */
    public String chooseNodeChannel() {
        if (nodeInfos.size() == 1) return getChannelKey(nodeInfos.get(0).getIp());
        if (nodeInfos.size() == 0) return null;
        return getChannelKey(nodeInfos.get(indexMap[nextIndex()]).getIp());
    }


    /**
     * 根据节点获取随机节点channelKey
     */
    public String getChannelKey(String node) {
        List<String> channelKeys = NodePoolCache.getAllNodeRpcSrvListByNode(node);
        if (CollectionUtils.isEmpty(channelKeys)) {
            /**很有可能当前节点服务全部挂掉*/
            throw new RPCException("######### 当前节点 node：" + node + " 服务不可用！！！");
        }
        int size = NodePoolCache.nodeRpcSize(node);
        int randomIndex = random.nextInt(size);
        int index = 0;
        for (String channelKey : channelKeys) {
            if (index == randomIndex) {
                return channelKey;
            }
            index++;
        }
        return null;
    }

    /**
     * 20s实时打印负载服务
     */
    public static void statisPrintLoadBalanceSrv() {

        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                for (NodeInfo nodeInfo : nodeInfos) {
                    logger.info("@@@@@@ rpcLoadBalance node:{}",
                            (nodeInfo.getIp() + ":" + nodeInfo.getPort()));
                }
            }
        }, 0, 20 * 1000, TimeUnit.MILLISECONDS);


    }


    /**
     * 获取节点索引index
     * 加锁,并发有安全问题
     */
    private synchronized int nextIndex() {
        weightIndex++;
        if (weightIndex >= weightSum) weightIndex = 0;
        return weightIndex;
    }

    /**
     * 刷新权重映射
     */
    public void initWeight() {
        weightSum = 0;

        for (NodeInfo nodeInfo : nodeInfos) weightSum += nodeInfo.getWeight();
        //权重值总和
        indexMap = new short[weightSum];

        //索引
        short index = 0;
        //偏移量
        int offset = 0;

        for (NodeInfo nodeInfo : nodeInfos) {
            for (int i = 0; i < nodeInfo.getWeight(); i++) {
                indexMap[i + offset] = index;
            }
            offset += nodeInfo.getWeight();
            index++;
        }
    }

}
