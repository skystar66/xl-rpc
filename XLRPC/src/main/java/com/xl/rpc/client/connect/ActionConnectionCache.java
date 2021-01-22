//package com.xl.rpc.client.connect;
//
//import com.xl.rpc.client.loadbalance.weight.ActionNodeWeight;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//
///**
// * @author xl
// * @date: 2020-12-18
// * @desc: 存储管理各个action 模块的节点权重上下文信息
// */
//@Slf4j
//public class ActionConnectionCache {
//
//    //按模块分类的rcpClient 集合
//    public static ConcurrentMap<String, ActionNodeWeight> actionRpcMap =
//            new ConcurrentHashMap<>();
//
//
//    /**
//     * 添加服务
//     */
//    public static ActionNodeWeight addActionRpcSrv(String action, ActionNodeWeight actionNodeWeight) {
//        return actionRpcMap.putIfAbsent(action, actionNodeWeight);
//    }
//
//    /**
//     * 根据action获取所有服务信息
//     */
//    public static ActionNodeWeight getActionNodeWeightByAction(String action) {
//        return actionRpcMap.get(action);
//    }
//
//
//    /**
//     * 移除服务节点
//     */
//    public static void removeActionRpcSrv(String action) {
//        ActionNodeWeight actionNodeWeight = actionRpcMap.get(action);
//        /**移除channel key*/
//        actionNodeWeight.removeNode();
//        /**移除action*/
//        actionRpcMap.remove(action);
//    }
//
//
//}
