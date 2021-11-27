package com.xl.rpc.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * 1.2 协议:
 __ __ __ __ __ __ __ __ __ ____ __ __ __ __ __ __ ____ __ __ _____ __ __ ____ __ __ __ __ __ __ __ __
 * |              |              |           |           |           |           |                         |
 *         2              4            1           1           4           1             Uncertainty
 * |__ __ __ __ __|__ __ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|_ __ __ __ __ __ __ __ __|
 * |              |              |           |           |           |           |                         |
 *        包尾        BodyLength       VER         Type         ID         ZIP             CONTENT
 * |__ __ __ __ __|__ __ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ ____ __ __ __ __|
 * */
public class Message implements Serializable {

    public final static int VER = 0;// 协议版本目前只有0 ,递增

    private byte type;

    private int id;

    private byte ver = VER;


    private byte zip;// 压缩类型: 0不压缩 1snappy 2gzip

    private byte[] content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getVer() {
        return ver;
    }

    public void setVer(byte ver) {
        this.ver = ver;
    }

    public byte getZip() {
        return zip;
    }

    public void setZip(byte zip) {
        this.zip = zip;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getString() {
        if (content == null)
            return null;
        return new String(content);
    }

    public void setString(String s) {
        if (s != null)
            this.content = s.getBytes();
    }

    public JSONObject getJSONObject() {
        if (content == null)
            return null;
        return JSON.parseObject(getString());
    }

    public void setJSONObject(JSONObject json) {
        if (json != null)
            setString(json.toJSONString());
    }

    private static int ID;

    public static synchronized int createID() {
        return ++ID;
    }

    public static synchronized void destoryID() {
        ID=0;
    }
    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", id=" + id +
                ", ver=" + ver +
                ", zip=" + zip +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
