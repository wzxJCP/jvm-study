package com.xing.jvmstudy.stack;

public class Test {
    public static void main(String[] args) {
        new Test().test();
    }

    public void test(){
        a();
    }

    public void a(){
        test();
    }
}

// Exception in thread "main" java.lang.StackOverflowError
