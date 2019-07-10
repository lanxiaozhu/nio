package com.example.demo.nio;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @Auther: wishm
 * @Date: 2019/6/30 21:12
 * @Description: io/nio io是流单向通道； nio 是双向通道，通道为 channel  装载为buffer(缓冲区)
 *      核心参数：
 *          Position 当前正在操作位置
 *          Limit 可操作数据大小
 *          Capacity 数据容量大小
 *          Mark 上次position的位置
 *
 *   通过allocate(); 获取 非直接缓冲区
 *   通过allocateDirect方法分配 直接缓冲区，将缓冲区建立在物理内存中，可提高效率
 *
 *   通过flip(); 切换为读模式
 *
 * 根据数据类型的不同，提供了相应类型的缓冲区；排除Boolean
 * ByteBuffer
 * IntBuffer
 * CharBuffer
 * ShortBuffer
 * LongBuffer
 * DoubleBuffer
 * FloatBuffer
 *
 * 数据操作：put(); get();  rewind();可重复读
 *
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NioFileChannel {
    /**
     * 读写基本操作
     */
    @Test
    public void allocate(){
        //1、通过allocate()分配缓冲区
        ByteBuffer allocate = ByteBuffer.allocate(1024);


        System.out.println("position:"+allocate.position());
        System.out.println("limit:"+allocate.limit());
        System.out.println("capacity"+allocate.capacity());


        String data = "我爱中国";//，每个汉字 占三个字节
        allocate.put(data.getBytes());
        System.out.println("----------------------data after put-------------------");
        System.out.println("position:"+allocate.position());
        System.out.println("limit:"+allocate.limit());
        System.out.println("capacity"+allocate.capacity());


        //切换读模式
        allocate.flip();

        //get数据存放到 数组中
        byte[] bytes = new byte[allocate.limit()];
        allocate.get(bytes);
        System.out.println(new String(bytes, 0, bytes.length));


    }

    /**
     * 通道 Channel
     */

    @Test
    public void channel() throws Exception {
        //1、创建流
        FileInputStream inputStream = new FileInputStream("1.txt");
        FileOutputStream outputStream = new FileOutputStream("2.txt");


        //2、获取通道
        FileChannel inputStreamChannel = inputStream.getChannel();
        FileChannel outputStreamChannel = outputStream.getChannel();

        //3、创建非直接缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        //4、将数据读取到 缓冲区
        while (inputStreamChannel.read(byteBuffer) != -1){
            //5、首先切换到·读模式
            byteBuffer.flip();

            //6、将数据写入到输出流
            outputStreamChannel.write(byteBuffer);

            byteBuffer.clear();//清空缓冲区数，再次循环写入
        }
        //流和通道需要关闭
        outputStreamChannel.close();
        inputStreamChannel.close();

        outputStream.close();
        inputStream.close();

    }
    /**
     * 直接缓冲区
     * 直接缓冲区虽快，但是 垃圾回收机制 并不能及时清理，导致程序抱死，因为 性能开销较大
     */
    @Test
    public void memoryMapped() throws Exception {
        //1.7获取通道的静态方法· open

        //Standard Open Option  标准操作


        //1、获取通道
        FileChannel inputChannel = FileChannel.open(Paths.get("1.txt"), StandardOpenOption.READ);

        FileChannel outputChannel = FileChannel.open(Paths.get("5.txt"),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                //创建 可覆盖 create_nwe 不可覆盖
                StandardOpenOption.CREATE);

        //2、获取内存映射
        MappedByteBuffer inputMap = inputChannel.map(FileChannel.MapMode.READ_ONLY, 0, inputChannel.size());
        MappedByteBuffer outputMap = outputChannel.map(FileChannel.MapMode.READ_WRITE, 0, inputChannel.size());


        //3、数据存取 如果 new byte[1024]; 但是我的数据没有到达1024 byte ,会抛出 BufferUnderflowException
        /**
         * DirectByteBuffer
         *  if (length > rem)
         *                 throw new BufferUnderflowException();
         */
        byte[] bytes = new byte[inputMap.limit()];
        inputMap.get(bytes);
        outputMap.put(inputMap);


        inputChannel.close();
        outputChannel.close();



    }


    /**
     * 直接缓冲区
     * 通道之间传输
     */
    @Test
    public void channelTransfer() throws Exception {
        //1.7获取通道的静态方法· open

        //Standard Open Option  标准操作


        //1、获取通道
        FileChannel inputChannel = FileChannel.open(Paths.get("1.txt"), StandardOpenOption.READ);

        FileChannel outputChannel = FileChannel.open(Paths.get("2.txt"),
                StandardOpenOption.WRITE,
                //创建 可覆盖 create_nwe 不可覆盖
                StandardOpenOption.CREATE);

        inputChannel.transferTo(0,inputChannel.size(),outputChannel);

        inputChannel.close();
        outputChannel.close();



    }
}
