package com.xing.jvmstudy.nativeDemo;

public class Demo {
    public static void main(String[] args) {
        new Thread(() ->{
        }, "t1").start();
    }
    // 这个Thread是一个类，这个方法定义在这里是不是很诡异！看这个关键字native；
    private native void test();
}
