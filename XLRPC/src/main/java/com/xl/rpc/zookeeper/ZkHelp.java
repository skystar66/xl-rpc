package com.xl.rpc.zookeeper;

import com.github.zkclient.IZkChildListener;
import com.github.zkclient.IZkDataListener;
import com.github.zkclient.ZkClient;
import com.xl.rpc.utils.AddressUtils;
import com.xl.rpc.zookeeper.bean.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Zookeeper API
 * 
 * @author xl
 * @version 2017年10月18日
 */
public class ZkHelp {
	private final static Logger logger = LoggerFactory.getLogger(
			ZkHelp.class);

	private static class InstanceHolder {
		private static final ZkHelp instance = new ZkHelp();
	}

	private ZkHelp() {
		//todo 先注释掉
		init();
	}

	public static ZkHelp getInstance() {
		return InstanceHolder.instance;
	}

	// 环境标志
	private String environmentFlag = "";
	private String publicKeyFile = "";

	public ZkClient client = null;
	private final static String envRegex = "(/\\w+){2}";
	// zookeeper集群地址 开发环境
	public String zooKeeperCluster = "";

	public int sessionTimeout = 60000;
	public int connectionTimeout = 60000;

	/**
	 * 初始化
	 */
	private void init() {
		try {
			// 测试环境
			zooKeeperCluster = getZkCluster();
			client = new ZkClient(zooKeeperCluster, sessionTimeout, connectionTimeout);
			environmentFlag = Env.getEnvironmentFlag();
			logger.info("environmentFlag={}", environmentFlag);
			if (environmentFlag == null || "".equals(environmentFlag)) {
				throw new RuntimeException("environmentFlag should not be empty");
			}
			if (!environmentFlag.matches(envRegex)) {
				throw new RuntimeException(environmentFlag + " is not a right environmentFlag :" + envRegex);
			}
			String rootPath = environmentFlag;
			this.addNode(rootPath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * thrift server 启动服务时候调增加临时节点
	 */
	public boolean regInCluster(String path, String serverName) {
		String newPath = path+ "/" + AddressUtils.getInnetIp();
		logger.info(">>>>> start register " + newPath + " in cluster <<<<<");
		setPathData(path, null);
		boolean b = createEphemeral(newPath, serverName);
		if (b) {
			logger.info("Servers:" + getChildren(path));
			logger.info(">>>>> register server " + serverName + " ok <<<<<");
		}
		return b;
	}


	/**
	 * 检查是否需要增加环境标示
	 *
	 * @param path 环境标示
	 * @return boolean
	 */
	private boolean checkEnv(String path) {
		return path.startsWith(environmentFlag);
	}


	private String getZkCluster() {
		String zooKeeperCluster = System.getProperty("config.zkCluster");
		if (zooKeeperCluster == null || "".equals(zooKeeperCluster)) {
			// 开发环境
			if (Env.isDev()) {
				zooKeeperCluster = "127.0.0.1:2181";
			}

			logger.info("zooKeeperCluster={}", zooKeeperCluster);
			return zooKeeperCluster;
		}
		return "";
	}
	/**
	 * 获取节点数据
	 */
	public String getValue(String path) {

		if (!checkEnv(path))
			path = environmentFlag + path;
		try {
			if (client.exists(path)) {
				byte data[] = client.readData(path);
				if (data == null) {
					return null;
				}
					return new String(data, "UTF-8");


			}
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}

		logger.info("zookeeper NoNode exists for " + path);
		return null;
	}



	/**
	 * 删除节点
	 */
	public void delete(String path) {
		try {
			if (!checkEnv(path))
				path = environmentFlag + path;


			if (client.exists(path) && !client.delete(path)) {
				throw new RuntimeException("zk delete node failed:" + path);
			}

		} catch (Exception e) {
			logger.error("zookeeper client  delete error!", e);
			throw new RuntimeException("delete exception:" + e.getMessage());
		}
	}

	/**
	 * 添加zk节点,默认是不加密节点
	 */
	public void addNode(String path) {
		this.addNode(path, false);
	}

	/**
	 * 添加zk节点
	 */
	public void addNode(String path, boolean isEncrypted) {
		try {
			if (!checkEnv(path))
				path = environmentFlag + path;
			if (client.exists(path)) {
				return;
			}
			// 初始化权限信息

			String tmpPath = "";
			String array[] = path.split("/");
			for (String anArray : array) {
				if (anArray.equals("")) {
					continue;
				}
				tmpPath = tmpPath + "/" + anArray;
				// 节点不存在先创建
				if (!client.exists(tmpPath)) {
					client.createPersistent(tmpPath, null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	/**
	 * 节点数据变化监听
	 */
	public void subscribeDataChanges(String path, IZkDataListener listener) {

		if (!checkEnv(path))
			path = environmentFlag + path;
		client.subscribeDataChanges(path, listener);
	}

	/**
	 * 节点子节点变化监听
	 */
	public void subscribeChildChanges(String path, IZkChildListener listener) {

		if (!checkEnv(path))
			path = environmentFlag + path;
		client.subscribeChildChanges(path, listener);
	}

	/**
	 * 获取path节点下的儿子节点列表
	 */
	public List<String> getChildren(String path) {
		if (!checkEnv(path))
			path = environmentFlag + path;
		if (client.exists(path)) {
			return client.getChildren(path);
		}
		logger.info("zookeeper NoNode exists for " + path);
		return new ArrayList<>();
	}


	/**
	 * 创建临时节点
	 */
	public boolean createEphemeral(String path, String value) {
		if (!checkEnv(path))
			path = environmentFlag + path;
		boolean b = false;
		try {
			client.createEphemeral(path, value.getBytes("UTF-8"));
			b = true;
		} catch (Exception e) {
			logger.error("!!!! register " + path + " in cluster error !!!", e);
		}
		return b;
	}

	/**
	 * 节点设置值
	 *
	 * @param path  路径
	 * @param value 值
	 */
	public void setPathData(String path, String value) {
		if (!checkEnv(path))
			path = environmentFlag + path;
		this.addNode(path);
		client.writeData(path, value == null ? null : value.getBytes(Charset.forName("UTF-8")));
	}


	/**
	 * 判断节点是否存在
	 *
	 */
	public boolean exists(String path) {
		if (!checkEnv(path))
			path = environmentFlag + path;
		return client.exists(path);
	}

	public String getPublicKeyFile() {
		return publicKeyFile;
	}

	public void setPublicKeyFile(String publicKeyFile) {
		this.publicKeyFile = publicKeyFile;
	}

}
