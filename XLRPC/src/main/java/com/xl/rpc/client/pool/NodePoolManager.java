package com.xl.rpc.client.pool;

import com.alibaba.fastjson.JSON;
import com.xl.rpc.client.RpcClient;
import com.xl.rpc.client.connect.ConnectionCache;
import com.xl.rpc.client.loadbalance.weight.RpcLoadBalance;
import com.xl.rpc.cluster.ClusterCenter;
import com.xl.rpc.config.ServerConfig;
import com.xl.rpc.exception.RPCException;
import com.xl.rpc.server.node.NodeBuilder;
import com.xl.rpc.zk.NodeInfo;
import com.xl.rpc.zookeeper.ZkHelp;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xuliang
 * @date 2019年3月18日 下午3:16:41
 * <p>
 * 链接zookeeper,建立连接池
 */
public class NodePoolManager {


    // 内部静态类方式
    private static class InstanceHolder {
        private static NodePoolManager instance = new NodePoolManager();
    }

    public static NodePoolManager getInstance() {
        return NodePoolManager.InstanceHolder.instance;
    }


    private static final Logger logger = LoggerFactory.getLogger(NodePoolManager.class);

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private ZkHelp zkHelp = ZkHelp.getInstance();


    /**
     * 初始化连接池
     */
    public void initNodePool() {

        /**获取节点列表*/
        /**这里解释一下：获取的是某个业务服务下的节点列表，在使用时，会配置zkPath 路径， 例如这是一个 用户服务 zkPath=.../user/....*/
        List<String> nodeDatas = zkHelp.getChildren(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH));
        List<NodeInfo> nodeInfos = new ArrayList<>();
        for (String nodeIp : nodeDatas) {
            try {
                /**获取当前节点数据*/
                String nodeData = zkHelp.getValue(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH) +
                        "/" + nodeIp);

                NodeInfo nodeInfo = JSON.parseObject(nodeData, NodeInfo.class);
                nodeInfos.add(nodeInfo);
                /**监听当前服务节点的变化*/
                ClusterCenter.getInstance().listenerServerRpcConfig(nodeIp);
            } catch (Exception e) {
                logger.error("onNodeDataChange.parseObject", e);
            }
        }
        /**初始化连接池及权重值变化*/
        initPoolAndWeight(nodeInfos);
    }


    /**
     * 初始化连接池 非使用zk方式
     */
    public void initNodePool(String action) {

        List<NodeInfo> nodeInfos = new ArrayList<>();

        try {
            /**获取当前节点数据*/
            NodeInfo nodeInfo = NodeBuilder.buildNode();
            String[] actions = new String[]{action};
            nodeInfo.setActions(actions);
            nodeInfos.add(nodeInfo);
        } catch (Exception e) {
            logger.error("onNodeDataChange.parseObject", e);
        }

        /**初始化连接池及权重值变化*/
        initPoolAndWeight(nodeInfos);
    }

//
//    /**
//     * 初始化连接并赋予权重值 非使用zookeeper方式
//     */
//    public void initPoolAndWeightNoZK(List<NodeInfo> nodeDatas) {
//        for (NodeInfo nodeInfo : nodeDatas) {
//            /**step1: 建立连接*/
//            ConnectionPoolFactory.getInstance().zkSyncRpcServer(nodeInfo);
//            RpcLoadBalance.getInstance().addNode(nodeInfo);
//        }
//        /**step3: 初始化权重数据*/
//        for (Map.Entry<String, RpcLoadBalance> e : ActionConnectionCache.actionRpcMap.entrySet()) {
//            e.getValue().initWeight();
//        }
//
//    }


    /**
     * 节点变更通知
     */
    public void onNodeChange(List<NodeInfo> nodeDatas) {
        try {
            lock.writeLock().lock();
            logger.info("onNodeDataChange->" + nodeDatas.size() + "=" + JSON.toJSONString(nodeDatas));
            /**初始化连接并赋予权重值*/
            initPoolAndWeight(nodeDatas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 初始化连接并赋予权重值
     */
    public void initPoolAndWeight(List<NodeInfo> nodeDatas) {
        for (NodeInfo nodeInfo : nodeDatas) {
            /**step1: 建立连接*/
            ConnectionPoolFactory.getInstance().zkSyncRpcServer(nodeInfo);
            /**step2: 添加服务节点信息*/
            RpcLoadBalance.getInstance().addNode(nodeInfo);
        }
        /**step3: 初始化服务节点权重*/
        RpcLoadBalance.getInstance().initWeight();
    }


    /**
     * 初始化连接并赋予权重值
     */
    public void initRpcPoolSize(List<NodeInfo> nodeDatas) {
        for (NodeInfo nodeInfo : nodeDatas) {
            /**step1: 建立连接*/
            ConnectionPoolFactory.getInstance().zkSyncRpcServer(nodeInfo);
//            /**step2: 添加服务节点信息*/
//            RpcLoadBalance.getInstance().addNode(nodeInfo);
        }
//        /**step3: 初始化服务节点权重*/
//        RpcLoadBalance.getInstance().initWeight();
    }


    /**
     * 根据选择服务器,支持权重
     */
    public RpcClient chooseRpcClient(String action) {
        try {
            lock.readLock().lock();
            String channelKey = RpcLoadBalance.getInstance().chooseNodeChannel();
            if (StringUtils.isEmpty(channelKey)) {
                logger.info(">>>>>>> channel 不存在，请检查服务是否发生异常！！！");
                throw new RPCException(" channel 不存在，请检查调用服务是否发生异常！！！");
            }
            logger.info(">>>>>>> current choose server node key :{} ", channelKey);
            return ConnectionCache.get(channelKey);

        } finally {
            lock.readLock().unlock();
        }

    }

}
