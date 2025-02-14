
package com.xl.rpc.client.connect;

import com.xl.rpc.client.RpcClient;
import com.xl.rpc.client.loadbalance.weight.RpcLoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author xl
 * @date: 2020-12-18
 * @desc: 负责管理各个节点对应的channel集合, 方便对节点尽心中心化管理，统一上线/下线
 */
@Slf4j
public class NodePoolCache {

    //节点连接池
    public static ConcurrentMap<String, CopyOnWriteArrayList<String>> nodePoolMap =
            new ConcurrentHashMap<>();


    /**
     * 获取节点的channel数量
     */
    public static int nodeRpcSize(String node) {
        if (nodePoolMap.containsKey(node)) {
            return nodePoolMap.get(node).size();
        }
        return 0;
    }

    /**
     * 添加服务
     */
    public static void addActionRpcSrv(String node, String key, RpcClient client) {

        synchronized (node.intern()) {
            CopyOnWriteArrayList<String> actionRpcList = getAllNodeRpcSrvListByNode(node);
            if (CollectionUtils.isEmpty(actionRpcList)) {
                actionRpcList = new CopyOnWriteArrayList<>();

            }
            actionRpcList.add(key);
            ConnectionCache.putIfAbsent(key, client);
            nodePoolMap.putIfAbsent(node, actionRpcList);
            show();
        }


    }


    /**
     * 获取所有节点服务信息
     */
    public static CopyOnWriteArrayList<String> getAllNodeRpcSrvListByNode(String node) {
        CopyOnWriteArrayList<String> actionRpcList = nodePoolMap.get(node);
        if (CollectionUtils.isEmpty(actionRpcList)) {
            return null;
        }
        return actionRpcList;
    }



    /**
     * 移除服务channel
     */
    public static void removeActionRpcSrv(String node, String channelKey) {
        synchronized (node.intern()) {
            List<String> actionRpcList = getAllNodeRpcSrvListByNode(node);
            if (CollectionUtils.isNotEmpty(actionRpcList) && actionRpcList.size() == 1) {
                /**当该节点所有的channel 都挂掉之后，移除节点*/
                actionRpcList.remove(channelKey);
                nodePoolMap.remove(node);
                RpcLoadBalance.getInstance().removeNode(node);
            }
            ConnectionCache.remove(channelKey);
            actionRpcList.remove(channelKey);
            show();
        }

    }


    /**
     * 移除节点
     */
    public static void removeAction(String node) {
        List<String> actionRpcList = getAllNodeRpcSrvListByNode(node);
        if (CollectionUtils.isNotEmpty(actionRpcList)) {
            for (String connectFlag : actionRpcList) {
                ConnectionCache.remove(connectFlag);
            }
        }
        nodePoolMap.remove(node);
    }

    public static void remove(String node) {
        nodePoolMap.remove(node);
        show();
    }

    /**
     * 展示连接数量
     */
    public static void show() {
        log.info("####### 当前节点数量: {},  当前节点连接池信息：{}", nodePoolMap.size(), nodePoolMap);


    }
}
