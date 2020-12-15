package com.xl.rpc.zookeeper.listener;//package com.netty.zookeeper.listener;
//
//import com.github.zkclient.IZkDataListener;
//import com.netty.zookeeper.ZkHelp;
//
//public class ZkDataListener implements IZkDataListener {
//  private ZkHelp zkHelp = ZkHelp.getInstance();
//  private ZkDataCallback callback;
//
//  public ZkDataListener(ZkDataCallback callback) {
//    this.callback = callback;
//  }
//
//  @Override
//  public void handleDataChange(String dataPath, byte[] data) throws Exception {
//    if (data != null) {
//      callback.handleDataChange(dataPath, zkHelp.isEncrpted(dataPath) ? zkHelp.decrypt(data)
//          : new String(data, "UTF-8"));
//    } else {
//      callback.handleDataChange(dataPath, null);
//    }
//  }
//
//  @Override
//  public void handleDataDeleted(String dataPath) throws Exception {
//    callback.handleDataDeleted(dataPath);
//  }
//
//  public interface ZkDataCallback {
//    void handleDataChange(String dataPath, String value) throws Exception;
//
//    void handleDataDeleted(String dataPath) throws Exception;
//  }
//}
