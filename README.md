# xl-rpc
---
一个自动注册扩展服务、netty长连接池的高性能轻量级RPC框架
<br/>

  * 使用zookeeper服务发现,自动注册扩展服务
  * 使用长连接TCP池,netty作为网络IO,支持全双工通信,高性能
  * 消息发送支持异步/同步,NIO
  * 自动选择符合action节点服务器,支持权重分发消息
  * 支持snappy,gzip压缩
  * 可进行二次封装开发,[远程调用][qsrpc-starter],消息路由负载均衡等等


