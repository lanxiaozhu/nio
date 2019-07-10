package com.example.demo.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Auther: wishm
 * @Date: 2019/7/3 15:41
 * @Description: 阻塞io / 服务端不返回数据 /服务端返回数据
 */
public class socket {

    /**
     * 客户端
     *
     * @throws Exception
     */
    @Test
    public void clinet() throws Exception {
        System.out.println("开启客户端");


        /**
         * 1、 网络流
         */
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 8090);
        SocketChannel socketChannel = SocketChannel.open(inetSocketAddress);


        //2、文件流
        FileChannel inputChannle = FileChannel.open(Paths.get("1.txt"), StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (inputChannle.read(buffer) != -1) {
            buffer.flip();
            //3、将文件流 数据 写入网络流
            socketChannel.write(buffer);

            buffer.clear();

        }
        inputChannle.close();
        socketChannel.close();

    }

    @Test
    public void serverSocket() throws Exception {
        System.out.println("开启服务端");

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8090));

        //堵塞  等待接收
        SocketChannel socketChannel = serverSocketChannel.accept();


        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (socketChannel.read(buffer) != -1) {
            buffer.flip();

            buffer.clear();


        }
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        System.out.println(new String(bytes, 0, bytes.length));


        socketChannel.close();

        serverSocketChannel.close();

    }


    @Test
    public void clientRetrun() throws IOException {
        SocketChannel open = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8090));


        FileChannel fileChannel = FileChannel.open(Paths.get("1.txt"), StandardOpenOption.READ);


        //通过缓冲区 赋值数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        while (fileChannel.read(byteBuffer) != -1) {
            byteBuffer.flip();

            open.write(byteBuffer);

            byteBuffer.clear();

        }

        //必须告知服务端 我已发送完成，我方才能够接收到数据
        open.shutdownOutput();
        //如何接收
        int lenth = 0;
        while ((lenth = open.read(byteBuffer)) != -1) {
            System.out.println("--");
            byteBuffer.flip();

            System.out.println(new String(byteBuffer.array(), 0, lenth));

            byteBuffer.clear();
        }


        fileChannel.close();
        open.close();
    }


    @Test
    public void serverSocketRetrun() throws IOException {
        ServerSocketChannel open = ServerSocketChannel.open();

        open.bind(new InetSocketAddress(8090));

        SocketChannel accept = open.accept();


        ByteBuffer bytes = ByteBuffer.allocate(1024);

        int lenth = 0;
        while ((lenth = accept.read(bytes)) != -1) {

            bytes.flip();
         //   System.out.println(new String(bytes.array(), 0, lenth));

            bytes.clear();
        }

        System.out.println("zhunb1");

        bytes.put("我爱中国".getBytes());
        bytes.flip();

        accept.write(bytes);

        accept.close();
        open.close();


    }
}
