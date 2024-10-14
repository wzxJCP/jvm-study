package com.xing.jvmstudy.heap;

import java.util.Random;

//+VM测试：-Xms8m -Xmx8m -XX:+PrintGCDetails
public class Test {
    public static void main(String[] args) {
        String str = "hello";
        while (true) {
            str += str + new Random().nextInt(323523234) + new Random().nextInt(678768568);
        }
    }
}
