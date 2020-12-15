package com.xl.rpc.client.pool;

import com.alibaba.fastjson.JSON;
import com.xl.rpc.client.RpcClient;
import com.xl.rpc.client.connect.ActionConnectionCache;
import com.xl.rpc.client.connect.ConnectionCache;
import com.xl.rpc.client.loadbalance.weight.ActionNodeWeight;
import com.xl.rpc.config.ServerConfig;
import com.xl.rpc.zk.NodeInfo;
import com.xl.rpc.zookeeper.ZkHelp;
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
        List<String> nodeDatas = zkHelp.getChildren(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH));
        List<NodeInfo> nodeInfos = new ArrayList<>();
        for (String node : nodeDatas) {
            try {
                /**获取当前节点数据*/
                String nodeData = zkHelp.getValue(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH)+
                        "/"+node);
                NodeInfo nodeInfo = JSON.parseObject(nodeData, NodeInfo.class);
                nodeInfos.add(nodeInfo);
            } catch (Exception e) {
                logger.error("onNodeDataChange.parseObject", e);
            }
        }
        /**初始化连接池及权重值变化*/
        initPoolAndWeight(nodeInfos);
    }

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
            /**step2: 把节点按action分组,也就是同样服务功能的服务器放一起*/
            String[] actions = nodeInfo.getActions();
            for (String action : actions) {
                ActionNodeWeight actionNodeContext = ActionConnectionCache.getActionNodeWeightByAction(action);
                if (actionNodeContext == null) {
                    actionNodeContext = new ActionNodeWeight();
                    ActionNodeWeight old = ActionConnectionCache.addActionRpcSrv(action, actionNodeContext);
                    if (old != null) {
                        actionNodeContext = old;
                    }
                }
                /**添加节点信息 加入到action weight中*/
                actionNodeContext.addNode(nodeInfo);
            }
        }
        /**step3: 初始化权重数据*/
        for (Map.Entry<String, ActionNodeWeight> e : ActionConnectionCache.actionRpcMap.entrySet()) {
            e.getValue().initWeight();
        }

    }


    /**
     * 根据action/ip选择服务器,支持权重
     */
    public RpcClient chooseRpcClient(String action) {
        try {
            lock.readLock().lock();
            ActionNodeWeight actionNodeContext = ActionConnectionCache.getActionNodeWeightByAction(action);
            if (actionNodeContext == null) {
                logger.info("chooseClientPool: can not find pool - " + action);
                return null;
            }
            String channelKey = actionNodeContext.nextNode();
            return ConnectionCache.get(channelKey);

        } finally {
            lock.readLock().unlock();
        }

    }

}
