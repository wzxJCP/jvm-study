package com.xing.jvmstudy;

public class Car {
    public static void main(String[] args) {
        // 类是模板，对象是具体的
        Car car1 = new Car();
        Car car2 = new Car();
        Car car3 = new Car();

        System.out.println(car1.hashCode());
        System.out.println(car2.hashCode());
        System.out.println(car3.hashCode());

        Class<? extends Car> aClass1 = car1.getClass();

        ClassLoader classLoader = aClass1.getClassLoader();

        System.out.println(classLoader); //AppClassLoader
        System.out.println(classLoader.getParent()); //PlatformClassLoader
        System.out.println(classLoader.getParent().getParent()); //null 1.不存在 2.java虚拟机没有这个类加载器取不到 rt.jar

    }
}

/**
 * 2084435065
 * 1896277646
 * 2128227771
 * jdk.internal.loader.ClassLoaders$AppClassLoader@4617c264
 * jdk.internal.loader.ClassLoaders$PlatformClassLoader@6576fe71
 * null
 */