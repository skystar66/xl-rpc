package com.xl.rpc.client.loadbalance.weight;

import com.xl.rpc.client.connect.NodePoolCache;
import com.xl.rpc.exception.RPCException;
import com.xl.rpc.zk.NodeInfo;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 负载均衡
 *
 * @desc 服务权重计算类
 */
public class ActionNodeWeight {


    private final List<NodeInfo> nodeInfos = new ArrayList<>();

    private short[] indexMap;//下标为weight,值为nodeInfos对应的节点index,提升选择性能
    private volatile int weightIndex = -1;
    private int weightSum;
    private Random random = new Random();

    public void addNode(NodeInfo nodeInfo) {
        if (!nodeInfos.contains(nodeInfo)) {
            nodeInfos.add(nodeInfo);
        }
    }

    public void removeNode() {
        for (NodeInfo nodeInfo : nodeInfos) {
            NodePoolCache.removeAction(nodeInfo.getIp());
        }
    }

    //根据权重获取节点
    public String nextNode() {
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
            throw new RPCException("######### 当前节点 node："+node+" 服务不可用！！！");
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


    //加锁,并发有安全问题
    private synchronized int nextIndex() {
        weightIndex++;
        if (weightIndex >= weightSum) weightIndex = 0;
        return weightIndex;
    }

    //刷新权重映射
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
