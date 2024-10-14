package com.xing.jvmstudy.heap;

import java.util.ArrayList;
// -Xms设置初始化内存分配大小 1/64
// -Xmx没置最大分配内存，默认1/4
// -Xms1024m -Xmx1024m -XX:+PrintGCDetails        //打印GC拉圾回收信息
// -Xms1m -Xmx8m -XX:+HeapDumpOnOutOfMemoryError  //oom DUMP
public class Test03 {

    byte[] array = new byte[1*1024*1024]; // 10M 1M=1024k
    public static void main(String[] args) {
        ArrayList<Test03> list = new ArrayList<>();
        int count = 0;
        try {
            while (true) {
                list.add(new Test03()); // 问题所在
                count = count+1;
            }
        } catch (Exception e) { //Error测试
            System.out.println("count:"+count);
            e.printStackTrace();
        }
    }
}

/**
 * count:335
 * java.lang.OutOfMemoryError: Java heap space
 */