package com.xl.rpc.cluster;

import com.alibaba.fastjson.JSON;
import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkDataListener;
import com.xl.rpc.client.pool.NodePoolManager;
import com.xl.rpc.config.ServerConfig;
import com.xl.rpc.zk.NodeInfo;
import com.xl.rpc.zookeeper.ZkHelp;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Zk Cluster管理
 *
 * @author xl
 * @version 2020年11月20日
 */
public class ClusterCenter {

    private Logger log = LoggerFactory.getLogger(ClusterCenter.class);

    private static ZkHelp zkHelp = ZkHelp.getInstance();

    // 内部静态类方式
    private static class InstanceHolder {
        private static ClusterCenter instance = new ClusterCenter();
    }

    public static ClusterCenter getInstance() {
        return InstanceHolder.instance;
    }


    public ClusterCenter() {

    }

    public List<String> serverRpcList = null;
    public String rpcPoolSize = null;


    /**
     * Server RPC连接
     */
    public void listenerServerRpc() {
        serverRpcList = zkHelp.getChildren(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH));
        log.info("serverRpcList:{}", serverRpcList);
        IZkChildListener listener = new IZkChildListener() {
            public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
                // 监听到子节点变化 更新cluster
                log.info("----->>>>> Starting handle children change " + parentPath + "/" + currentChildren + " size=" + currentChildren.size());
                serverRpcList = currentChildren;
                List<NodeInfo> nodeInfos = new ArrayList<>();
                for (String node : currentChildren) {
                    try {
                        String nodeData =zkHelp.getValue(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH)+
                                "/"+node);
                        if (StringUtils.isEmpty(nodeData)) {
                            return;
                        }
                        NodeInfo nodeInfo = JSON.parseObject(nodeData, NodeInfo.class);
                        if (nodeInfo != null) nodeInfos.add(nodeInfo);
                    } catch (Exception e) {
                        log.error("onNodeDataChange.parseObject", e);
                    }
                }
                NodePoolManager.getInstance().onNodeChange(nodeInfos);
            }
        };
        // 监控节点变更
        zkHelp.subscribeChildChanges(ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH)
                , listener);
    }


    /**
     * Server RPC连接池监控
     */
    public void listenerServerRpcPoolSize() {
        rpcPoolSize = zkHelp.getValue(ServerConfig.KEY_RPC_POOL_SIZE);
        log.info("serverRpcPoolSize:{}", serverRpcList);
        IZkDataListener listener = new IZkDataListener() {
            @Override
            public void handleDataChange(String parentPath, byte[] bytes) throws Exception {
                rpcPoolSize = new String(bytes);
                // 监听到子节点变化 更新cluster
                log.info("----->>>>> Starting rpcPoolSize data change " + parentPath + " rpcPoolSize=" + rpcPoolSize);
//                eventListener.rpcPoolChange(Integer.parseInt(rpcPoolSize));
            }
            @Override
            public void handleDataDeleted(String s) throws Exception {
            }
        };
        // 监控节点变更
        zkHelp.subscribeDataChanges(ServerConfig.KEY_RPC_POOL_SIZE, listener);
    }

    /**
     * 获取Live服务IP
     * 根据hashCode取余
     *
     * @param flag
     * @return
     */
    public String getServerIp(long flag) {
        long num = Math.abs(flag) % serverRpcList.size();
        String ip = serverRpcList.get((int) num);
        return ip;
    }


}