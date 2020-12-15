package com.xl.rpc.listener;

public interface MessageListener {


    byte[] onMessage(final byte[] message);

}
