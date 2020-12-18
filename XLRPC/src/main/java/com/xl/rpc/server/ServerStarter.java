package com.xl.rpc.server;

import com.alibaba.fastjson.JSON;
import com.xl.rpc.config.ServerConfig;
import com.xl.rpc.listener.MessageListener;
import com.xl.rpc.register.NodeBuilder;
import com.xl.rpc.server.TcpServer;
import com.xl.rpc.zk.NodeInfo;
import com.xl.rpc.zookeeper.ZkHelp;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端启动类
 *
 * @author xl
 * @date 2020-12-08
 */
@Slf4j
public class ServerStarter implements Runnable {


    private MessageListener messageListener;

    private NodeInfo nodeInfo;

    public ServerStarter(NodeInfo nodeInfo, MessageListener messageListener) {

        this.messageListener = messageListener;
        this.nodeInfo = nodeInfo;

    }

    @Override
    public void run() {
        starter();
    }


    public void starter() {
        TcpServer tcpServer = new TcpServer(nodeInfo, messageListener);
        boolean isStart = tcpServer.start();
        if (isStart) {
            /**是否注册zk长连接服务*/
            boolean isReg = ServerConfig
                    .getBooleanNotnull(ServerConfig.KEY_RPC_REGISTER_ZOOKEEPER);
            log.info("###### 是否注册zookeeper 长连接服务节点 isReg : 【{}】",isReg?"是":"否");
            if (isReg) {
                /**注册zk服务节点*/
                ZkHelp.getInstance().regInCluster(nodeInfo.getZkRpcPath(), JSON.toJSONString(nodeInfo));
            }
        }
    }
}
