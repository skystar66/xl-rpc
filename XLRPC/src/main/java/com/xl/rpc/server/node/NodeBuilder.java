package com.xl.rpc.server.node;

import com.xl.rpc.config.ServerConfig;
import com.xl.rpc.utils.RPCConstants;
import com.xl.rpc.zk.NodeInfo;
import org.springframework.util.StringUtils;

public class NodeBuilder {


    public static NodeInfo buildNode() {
        //zk 地址
        String zkIps = ServerConfig.getStringNotnull(ServerConfig.KEY_RPC_ZK_IPS);
        //zk rpc 服务路径
        String zkPath = ServerConfig.getString(ServerConfig.KEY_RPC_ZK_PATH);
        // 默认zk rpc 路径
        if (StringUtils.isEmpty(zkPath)) zkPath = RPCConstants.DEFAULT_XL_RPC;

        //服务节点ip
        String node_ip = ServerConfig.getStringNotnull(ServerConfig.KEY_RPC_NODE_IP);
        //端口号
        int port = Integer.parseInt(ServerConfig.getStringNotnull(ServerConfig.KEY_RPC_NODE_PORT));
        int rpcPoolSize =Integer.parseInt( ServerConfig.getStringNotnull(ServerConfig.KEY_RPC_POOL_SIZE));
        //节点功能模块分类
        String node_action = ServerConfig.getString(ServerConfig.KEY_RPC_NODE_ACTION);
        //节点权重
        String weight = ServerConfig.getString(ServerConfig.KEY_RPC_NODE_WEIGHT);
        //初始化节点信息类
        NodeInfo nodeInfo = new NodeInfo();
        if (node_action != null) {
            nodeInfo.setActions(node_action.split(","));
        }
        nodeInfo.setIp(node_ip);
        nodeInfo.setPort(port);
        if (weight != null) {
            nodeInfo.setWeight(Byte.parseByte(weight));
        }
        nodeInfo.setZkIps(zkIps);
        nodeInfo.setZkRpcPath(zkPath);
        nodeInfo.setCoreThread(ServerConfig.getInt(ServerConfig.KEY_RPC_NODE_THREAD, Runtime.getRuntime().availableProcessors() * 2));
        nodeInfo.setZip(ServerConfig.getString(ServerConfig.KEY_RPC_NODE_ZIP));
        nodeInfo.setRpcPoolSize(rpcPoolSize);
        nodeInfo.setRetrySize(3);
        return nodeInfo;
    }

}
