package com.xl.rpc.queue;


import lombok.Data;

@Data
public class MessageEvent<T> {


    private T msg;


}
