package com.xlrpc.zookeeper.bean;

/**
 * 环境定义
 */
public enum Env {
  DEV("dev"), ;
  // 成员变量
  private String name;

  // 构造方法
  Env(String name) {
    this.name = name;
  }

  public static String getEnvironmentFlag() {
    return System.getProperty("config.type");
  }


  private static boolean isEmpty(String str) {
    return str == null || "".equals(str);
  }

  // 是否开发环境
  public static boolean isDev() {
    String configType = getEnvironmentFlag();
    return isEmpty(configType) || configType.contains(DEV.getName());
  }

  public static Env getCurrentEnv() {
    if (isDev()){
      return Env.DEV;
    }

    return Env.DEV;
  }

  public String getName() {
    return name;
  }
}