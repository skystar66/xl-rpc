package com.xl.rpc.zip;

import java.io.IOException;

/**
 * Created by xl
 * Date 2020/11/10
 */
public interface IZip {

    byte[] compress(byte[] bytes) throws IOException;

//    byte[] compress(byte[] bytes, ByteBuf byteBuf) throws IOException;

    byte[] uncompress(byte[] bytes) throws IOException;

}
