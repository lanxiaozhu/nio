package com.example.demo.nio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @Auther: wishm
 * @Date: 2019/7/9 12:21
 * @Description:  如果想要封装nio得使用 https://blog.csdn.net/qq_36666651/article/details/80955398
 */
public class nioNotBlock {
    public static void main(String[] args) throws IOException {
        //1、网络链接
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8081));
        //设置为 非阻塞模式
        socketChannel.configureBlocking(false);
        //分配缓冲区
        ByteBuffer by = ByteBuffer.allocate(1024);


        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String next = scanner.next();

            String req = "我来了" + next;
            System.out.println(req);
            by.put(req.getBytes());
            //切换读模式
            by.flip();
            //将缓冲区数据写入 通道
            socketChannel.write(by);
            //清空缓冲区
            by.clear();
        }
        //关闭通道
        socketChannel.close();
    }


    @Test
    public void server() throws IOException {
        //1、打开服务端 绑定端口
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(8081));
        //2、设置为非阻塞
        serverSocketChannel.configureBlocking(false);

        //3、获取选择器
        Selector selector = Selector.open();
        //4、注册到selector  监听接收事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        //5、从选择器轮询处理
        while (selector.select() > 0) {
            System.out.println("-------------轮询-------");

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                //接收就绪
                if (selectionKey.isAcceptable()) {
                    System.out.println("-------------接受就绪-------");

                    SocketChannel socketChannel = serverSocketChannel.accept();

                    socketChannel.configureBlocking(false);

                    socketChannel.register(selector, SelectionKey.OP_READ);
                    System.out.println("建立请求......");
                }
                if (selectionKey.isReadable()) {
                    //接收请求 就绪

                    System.out.println("-------------接受读请求-------");

                    //接收数据得 通道 在选择器中获取
                    SocketChannel channel = (SocketChannel) selectionKey.channel();

                    ByteBuffer by = ByteBuffer.allocate(1024);

                    int len = 0;
                    while ((len = channel.read(by)) > 0) {

                        by.flip();

                        System.out.println("接收数据:" + new String(by.array(), 0, len));


                        //没读到内容关闭
//                        socketChannel.shutdownOutput();
//                        socketChannel.shutdownInput();
                        by.clear();
                        System.out.println("连接断开......");
                    }
                }
                //删除注册key
                iterator.remove();
            }
        }

    }
}
