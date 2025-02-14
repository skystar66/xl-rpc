package com.xl.rpc.server.manager;

import io.netty.channel.Channel;

import java.util.concurrent.CopyOnWriteArrayList;

public class ChannelManager {


    public static CopyOnWriteArrayList<Channel> channelList = new CopyOnWriteArrayList<>();


    public static void addChannel(Channel channel) {
        channelList.add(channel);
        System.out.println("channelList size:" + channelList.size());
    }

    public static void removeChannel(Channel channel) {
        channelList.remove(channel);
        System.out.println("channelList size:" + channelList.size());
    }

    public static int getChannelSize() {
        return channelList.size();
    }


    //随机取一个channel
    public static Channel getRandomChannel() {
        return channelList.get((int) (Math.random() * channelList.size()));
    }


}
