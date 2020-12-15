package com.xl.rpc.helper;

import com.alibaba.fastjson.JSONObject;
import com.github.zkclient.IZkDataListener;
import com.xl.rpc.config.CommonConfig;
import com.xl.rpc.utils.BeanJsonUtil;
import com.xl.rpc.utils.RPCConstants;
import com.xl.rpc.zookeeper.ZkHelp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * zk 动态监听配置
 *
 * @author xl
 * @version 2020年11月23日
 */
public class ZkConfigHelper {

    private final static Logger log = LoggerFactory.getLogger(ZkConfigHelper.class);

    private final static String configZkPath = RPCConstants.CONFIG;
    private final static String rpcPoolSizeZkPath = RPCConstants.SERVER_RPC_POOL_SIZE;

    private CommonConfig commonConfig = null;
    private Integer rpcPoolSize = null;
    private ZkHelp zkHelp = ZkHelp.getInstance();


    private static IZkDataListener listenerGlobal = null;
    private static IZkDataListener listenerRpcPoolSizeGlobal = null;

    private static class InstanceHolder {
        private static final ZkConfigHelper instance = new ZkConfigHelper();

    }

    public static ZkConfigHelper getInstance() {
        return InstanceHolder.instance;
    }
    private ZkConfigHelper() {
        listenerGlobal = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, byte[] data) throws Exception {
                log.info("!!! configZkPath node data has been changed !!!" + dataPath);
                    String rtdata = null;
                    if (data != null && data.length > 0) {
                        rtdata = new String(data, "UTF-8");
                        JSONObject json = JSONObject.parseObject(rtdata);
                        // read imconfig
                        String configNode = json.getString("imconfig");
                        commonConfig = BeanJsonUtil.toBean(configNode, CommonConfig.class);
                    }
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
            }
        };
        listenerRpcPoolSizeGlobal = new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, byte[] data) throws Exception {
                log.info("!!! rpcPoolSizeZkPath node data has been changed !!!" + dataPath);
                if (data != null && data.length > 0) {
                    String rpcPoolSizeStr = new String(data, "UTF-8");
                    rpcPoolSize = Integer.parseInt(rpcPoolSizeStr);
                }
            }
            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
            }
        }
        ;
        // 添加节点监控
        zkHelp.subscribeDataChanges(configZkPath, listenerGlobal);
        zkHelp.subscribeDataChanges(rpcPoolSizeZkPath, listenerRpcPoolSizeGlobal);
        try {

            String rtdata = new String(zkHelp.getValue(configZkPath));
            JSONObject json = JSONObject.parseObject(rtdata);


            // read imconfig
            String configNode = json.getString("imconfig");
            commonConfig = BeanJsonUtil.toBean(configNode, CommonConfig.class);


            String rpcPoolSizeStr = new String(zkHelp.getValue(rpcPoolSizeZkPath));
            rpcPoolSize = Integer.parseInt(rpcPoolSizeStr);

        } catch (
                Exception e) {
            log.error("", e);
        }
        log.info("===================init ZkConfigHelper ok================");
    }

    public CommonConfig getCommonConfig() {
        return commonConfig;
    }

    public void setCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    public ZkHelp getZkHelp() {
        return zkHelp;
    }

    public void setZkHelp(ZkHelp zkHelp) {
        this.zkHelp = zkHelp;
    }


    public Integer getRpcPoolSize() {
        return rpcPoolSize;
    }

    public void setRpcPoolSize(Integer rpcPoolSize) {
        this.rpcPoolSize = rpcPoolSize;
    }
}