package com.xl.rpc.starter.common.utils;


public class BeanNameUtil {
    private static class InstanceHolder {
        public static final BeanNameUtil instance = new BeanNameUtil();
    }
    public static BeanNameUtil getInstance() {
        return BeanNameUtil.InstanceHolder.instance;
    }

    private static String Seq = "-";



    public String getServiceBeanName(String interfaceName,
                                   String serviceBean,String version){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(interfaceName).append(Seq)
                .append(serviceBean).append(Seq).append(version);
        return stringBuilder.toString();
    }



}
