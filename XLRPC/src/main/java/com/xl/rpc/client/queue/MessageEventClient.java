package com.xl.rpc.client.queue;


import lombok.Data;

@Data
public class MessageEventClient<T> {


    private T msg;


}
