package com.xl.rpc.listener;

/**
 * @author xl
 * @desc: 消息监听处理器
 * @date: 2020-12-18
 */
public interface MessageListener {


    byte[] onMessage(final byte[] message);

}
