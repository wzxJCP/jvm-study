package com.xing.jvmstudy.heap;

public class JvmTest {
    public static void main(String[] args) {
        // 返回虚拟机试图使用的最大内存
        long max = Runtime.getRuntime().maxMemory(); // 字节：1024*1024
        // 返回jvm的总内存
        long total = Runtime.getRuntime().totalMemory();
        System.out.println("max=" + max + "字节\t" + (max /(double)1024 / 1024) + "MB");
        System.out.println("total=" + total + "字节\t" + (total/(double)1024 / 1024) + "MB");
        /** 认情况下:分配的总内存是电脑内存的1/4,初始化的内存是电脑的1/64
         * 电脑内存：15.7GB 当前使用12.9GB
         * max=4223664128字节	4028.0MB
         * total=264241152字节	252.0MB
         *
         * +VM测试：-Xms1024m -Xmx1024m -XX:+PrintGCDetails
         * max=1073741824字节	1024.0MB
         * total=1073741824字节	1024.0MB
         * 1048576K ÷ 1024 = 1024.0 MB
         */
    }
}
