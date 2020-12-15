package com.xl.rpc.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServerConfig {

    public final static String KEY_RPC_MESSAGE_MAXLEN = "qsrpc.message.maxlen";


    private static final String KEY_PREFIX="xlrpc.";



    public static final int VALUE_MAXLEN;



    public final static Properties properties;

    /**==============================zk相关配置信息==============================*/

    public static final String KEY_RPC_ZK_IPS = KEY_PREFIX+"zk.ips";
    public static final String KEY_RPC_ZK_PATH = KEY_PREFIX+"zk.path";

    /**==============================服务节点相关配置信息==============================*/
    public static final String KEY_RPC_NODE_IP = KEY_PREFIX+"node.ip";
    public static final String KEY_RPC_NODE_PORT = KEY_PREFIX+"node.port";
    public static final String KEY_RPC_POOL_SIZE = KEY_PREFIX+"node.rpc.size";
    public static final String KEY_RPC_NODE_ACTION = KEY_PREFIX+"node.action";
    public static final String KEY_RPC_NODE_WEIGHT = KEY_PREFIX+"node.weight";
    //服务端工作线程数/客户端pool.maxidle
    public static final String KEY_RPC_NODE_THREAD = KEY_PREFIX+"node.thread";
    public final static String KEY_RPC_NODE_ZIP = KEY_PREFIX+"node.zip";
    public static final String KEY_RPC_CONNECT_TIMEOUT = KEY_PREFIX+"connect.timeout";


    /**文件路径*/
    private final static String PROPRETIES_PATH = "/application.properties";



    static {
        properties = new Properties();
        try {
            InputStream is = Object.class.getResourceAsStream(PROPRETIES_PATH);
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        VALUE_MAXLEN = Integer.parseInt(getString(ServerConfig.KEY_RPC_MESSAGE_MAXLEN, 1024 * 1024 * 32 + ""));
    }




    public static String getString(String key, String def) {
        if (containsKey(key))
            return getString(key);
        else
            return def;
    }

    public static String getString(String key) {
        return properties.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public static int getInt(String key, int def) {
        if (containsKey(key))
            return getInt(key);
        else
            return def;
    }


    // 服务器配置必须存在,否则运行异常,防止BUG
    public static String getStringNotnull(String key) {
        String value = properties.getProperty(key);
        if (value == null)
            throw new RuntimeException(key + " property value is null");
        return value;
    }

    public static boolean containsKey(String key) {
        return properties.containsKey(key);
    }

}
