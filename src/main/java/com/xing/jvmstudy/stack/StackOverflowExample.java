package com.xing.jvmstudy.stack;

public class StackOverflowExample {

    public static void main(String[] args) {
        try {
            recursiveMethod();
        } catch (StackOverflowError e) {
            System.err.println("发生了栈溢出: " + e.getMessage());
        }
    }

    public static void recursiveMethod() {
        // 这里没有递归终止条件，将一直调用自身直到栈空间耗尽
        recursiveMethod();
    }
}