package com.xlRpc.kafka.client.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaticsVo {

    private int threadCnt;
    private int batchSize;
    //    private long totalMsgCnt;
//    private long totalTime;
    private double throughput;
    //平均消息延时
    private double avgTime;
    //成功率
    private double successRate;
    //失败数
    private long failCnt;
    //失败率
    private double failRate;
    //成功数
    private long successCnt;


}
