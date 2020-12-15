package com.xl.rpc.enums;

public enum MsgType {


    HEAT_CMD(1,"心跳检测"),;

    private int type;
    private String name;

    MsgType(int type, String name) {
        this.type = type;
        this.name = name;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
