package com.service.im;

import com.service.im.protobuf.Protobuf;

import java.net.Socket;

public class Test {

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("127.0.0.1", 6969);
        socket.getOutputStream().write(
                Protobuf.Body.newBuilder()
                        .setSender(1)
                        .setId("2")
                        .setType(3)
                        .build().toByteArray()
        );
        socket.getOutputStream().flush();
        socket.close();
    }

}
