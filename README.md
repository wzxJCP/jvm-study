# JVM（java虚拟机）

**面试常见：**

1. 请你谈谈你对JVM的理解?
2. java8虚拟机和之前的变化更新?
3. 什么是OOM，什么是栈溢出StackOverFlowError? 怎么分析?
4. JVM的常用调优参数有哪些?
5. 内存快照如何抓取？怎么分析Dump文件？
6. 谈谈JVM中，类加载器你的认识？

## 1.JVM的位置

![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\1.png)

**三种JVM:**

- Sun公司：HotSpot 用的最多
- BEA：JRockit
- IBM：J9VM

我们学习都是：HotSpot。

## 2.JVM的体系结构

![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\2.png)

- **jvm调优：99%都是在方法区和堆，大部分时间调堆。** JNI（java native interface）本地方法接口。

![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\3.jpg)

## 3.类加载器

- 作用：加载Class文件——如果new Student()；（具体实例在堆里，引用变量名放栈里） 。
- 先来看看一个类加载到 JVM 的一个基本结构：
  ![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\4.png)

- 类是模板，对象是具体的，通过new来实例化对象。car1，car2，car3，名字在栈里面，真正的实例，具体的数据在堆里面，栈只是引用地址。

1. 虚拟机自带的加载器
2. 启动类（根）加载器
3. 扩展类加载器
4. 应用程序加载器

```java
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
```

**类加载器的分类**

- Bootstrap ClassLoader 启动类加载器
- Extention ClassLoader 标准扩展类加载器
- Application ClassLoader 应用类加载器
- User ClassLoader 用户自定义类加载器

## 4.双亲委派机制

JVM的双亲委派机制是一种类加载机制，用于保证Java程序的安全性和稳定性。其核心思想是，当一个类加载器收到了类加载的请求时，它首先不会自己尝试去加载这个类，而是把这个请求委派给父类加载器去完成。每一层级的类加载器都是如此，因此所有的类加载请求最终都应该传递到最顶层的启动类加载器（Bootstrap Class Loader）。只有当父类加载器反馈自己无法完成这个加载请求（即在其搜索范围内未找到所需的类）时，子加载器才会尝试自己去加载。

4.1 双亲委派机制的主要组成部分

1. **启动类加载器 (Bootstrap Class Loader)**: 负责加载Java的核心库，如`java.lang.*`等。它是用原生代码实现的，位于JVM内部，因此不能被Java程序直接访问或引用。
2. **扩展类加载器 (Extension Class Loader)**: 负责加载标准扩展库中的类，例如放置在`$JAVA_HOME/lib/ext`目录下的jar包。它是由Java实现的，可以被程序引用。
3. **应用程序类加载器 (Application Class Loader)**: 也被称为系统类加载器，负责加载应用程序类路径（classpath）下的类文件。通常，这是加载用户应用程序类的默认类加载器。
4. **用户自定义类加载器 (User-defined Class Loaders)**: 开发者可以根据需要自定义类加载器，以实现特定的功能，如加载网络上的类文件、加密的类文件等。这些类加载器通常继承自`java.lang.ClassLoader`类，并重写`findClass`方法来实现自定义的类加载逻辑。

4.2 双亲委派机制的优点

- **安全性**：确保了核心类库不会被替换或篡改，比如`java.lang.String`类，无论哪个加载器加载这个类，都会由启动类加载器来加载，确保了所有环境中`String`类的行为一致性。
- **防止重复加载**：对于同一个类，只会被加载一次，避免了类的重复加载，节省了内存资源。
- **模块化管理**：有助于实现类的隔离，不同类加载器加载的类即使具有相同的全限定名也被视为不同的类，这对于构建模块化应用非常重要。

4.3 实现方式

双亲委派机制是通过`java.lang.ClassLoader`类中的`loadClass`方法实现的。当调用`loadClass`方法加载一个类时，它首先检查这个类是否已经被加载过；如果没有，它会调用父类加载器的`loadClass`方法；如果父类加载器为空，则表示到达了启动类加载器，此时会尝试使用启动类加载器加载该类；如果父类加载器加载失败，才会调用当前类加载器的`findClass`方法自行加载该类。

这种机制确保了类加载的过程既高效又安全，是Java平台能够成功运行的重要基石之一。

```java
package lang;

public class String {
    /*
    双亲委派机制:安全
    1.APP-->EXC-->BOOT(最终执行)
    BOOT
    EXC
    APP
     */
    public java.lang.String toString() {
        return "Hello";
    }

    public static void main(String[] args) {
        String s = new String();
        System.out.println(s.getClass());
        s.toString();
    }
    /*
    1.类加载器收到类加载的请求
    2.将这个请求向上委托给父类加载器去完成，一直向上委托，知道启动类加载
    3.启动加载器检查是否能够加载当前这个类，能加载就结束，使用当前的加载器，否则，抛出异常，适知子加载器进行加载
    4.重复步骤3
     */
}
```

- idea报了一个错误：

```java
错误: 在类 com.xing.jvmstudy.Car 中找不到 main 方法, 请将 main 方法定义为:
   public static void main(String[] args)
否则 JavaFX 应用程序类必须扩展javafx.application.Application

Process finished with exit code 1
```

> 这是因为，在运行一个类之前，首先会在应用程序加载器(APP)中找，如果APP中有这个类，继续向上在扩展类加载器EXC中找，然后再向上，在启动类( 根 )加载器BOOT中找。如果在BOOT中有这个类的话，最终执行的就是根加载器中的。如果BOOT中没有的话，就会倒找往回找。

**过程总结**

- 1.类加载器收到类加载的请求；
- 2.将这个请求向上委托给父类加载器去完成，一直向上委托，直到启动类加载器；
- 3.启动类加载器检查是否能够加载当前这个类，能加载就结束，使用当前的加载器，否则，抛出异常，一层一层向下，通知子加载器进行加载；
- 4.重复步骤3。

关于**双亲委派机制**的博客：

[你确定你真的理解“双亲委派“了吗？！](https://hollis.blog.csdn.net/article/details/112462198?utm_medium=distribute.pc_relevant.none-task-blog-2~default~BlogCommendFromMachineLearnPai2~default-11.control&dist_request_id=1329188.24840.16179780332075587&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~BlogCommendFromMachineLearnPai2~default-11.control)

[面试官：java双亲委派机制及作用](https://www.jianshu.com/p/1e4011617650)

- 概念：当某个类加载器需要加载某个.class文件时，它首先把这个任务委托给他的上级类加载器，递归这个操作，如果上级的类加载器没有加载，自己才会去加载这个类。
- 例子：当一个Hello.class这样的文件要被加载时。不考虑我们自定义类加载器，首先会在AppClassLoader中检查是否加载过，如果有那就无需再加载了。如果没有，那么会拿到父加载器，然后调用父加载器的loadClass方法。父类中同理也会先检查自己是否已经加载过，如果没有再往上。注意这个类似递归的过程，直到到达Bootstrap classLoader之前，都是在检查是否加载过，并不会选择自己去加载。直到BootstrapClassLoader，已经没有父加载器了，这时候开始考虑自己是否能加载了，如果自己无法加载，会下沉到子加载器去加载，一直到最底层，如果没有任何加载器能加载，就会抛出ClassNotFoundException。

![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\5.png)

作用：

1. 防止重复加载同一个.class。通过委托去向上面问一问，加载过了，就不用再加载一遍。保证数据安全。
2. 保证核心.class不能被篡改。通过委托方式，不会去篡改核心.class，即使篡改也不会去加载，即使加载也不会是同一个.class对象了。不同的加载器加载同一个.class也不是同一个Class对象。这样保证了Class执行安全。

> 比如：如果有人想替换系统级别的类：String.java。篡改它的实现，在这种机制下这些系统的类已经被Bootstrap classLoader加载过了（为什么？因为当一个类需要加载的时候，最先去尝试加载的就是BootstrapClassLoader），所以其他类加载器并没有机会再去加载，从一定程度上防止了危险代码的植入。

## 5.沙箱安全机制

沙箱安全机制是一种广泛应用于计算机科学领域的安全技术，主要用于提供一个受控的环境，在这个环境中可以安全地运行不可信的程序或代码。这种机制的主要目的是为了防止恶意软件或存在漏洞的软件对用户的计算机系统造成损害，比如数据泄露、系统崩溃等问题。沙箱通过限制程序的运行权限和资源访问能力，确保即使程序中包含有恶意代码，这些代码的影响也能被限制在沙箱内部，而不会扩散到宿主系统或其他应用程序中。

**沙箱安全机制的关键特点包括：**

1. **隔离性**：沙箱环境与其他系统资源严格隔离，确保沙箱内的活动不会直接影响到宿主系统。每个应用程序或进程在沙箱中都有自己的独立空间，无法直接访问或修改其他应用程序的数据或系统设置。
2. **受限权限**：在沙箱中运行的程序通常只有有限的权限，它们不能执行某些可能危及系统安全的操作，比如修改系统文件、安装驱动程序或与其他未授权的网络连接。
3. **安全性**：沙箱机制可以有效阻止恶意软件的传播，减少安全漏洞被利用的风险，提高整个系统的安全性。
4. **灵活性**：虽然沙箱提供了严格的限制，但它也足够灵活，允许开发者根据需要调整安全策略，以便在保证安全的同时，满足特定的功能需求。
5. **易于恢复**：如果沙箱中的程序导致了错误或异常，通常可以通过简单地重置沙箱来恢复，而不需要对整个系统进行复杂的修复工作。

**应用场景**

- **软件开发与测试**：开发人员可以在沙箱环境中测试新功能或第三方库，而不必担心可能对生产环境造成的负面影响。
- **Web浏览器**：现代Web浏览器通常使用沙箱技术来隔离网页脚本，防止恶意代码访问用户数据或控制系统。
- **移动操作系统**：Android和iOS等移动操作系统为每个应用程序提供了一个独立的沙箱环境，确保应用程序只能访问自己所需的最小权限集，从而保护用户隐私和数据安全。
- **企业级安全解决方案**：许多企业和组织使用沙箱来检测和分析可疑文件或行为，帮助识别新的威胁。

总之，沙箱安全机制是保障计算环境安全的重要工具之一，它通过提供一个安全、可控的执行环境，有效降低了恶意软件和漏洞利用的风险。

 在]ava中将执行程序分成本地代码和远程代码两种，本地代码默认视为可信任的，而远程代码则被看作是不受信的。对于授信的本地代码，可以访问一切本地资源。而对于非授信的远程代码在早期的ava实现中，安全依赖于沙箱(Sandbox)机制。如下图所示JDK1.0安全模型。

![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\6.png)

 但如此严格的安全机制也给程序的功能扩展带来障碍，比如当用户希望远程代码访问本地系统的文件时候，就无法实现。因此在后续的Java1.1 版本中，针对安全机制做了改进，增加了安全策略，允许用户指定代码对本地资源的访问权限。如下图所示JDK1.1安全模型。

![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\7.png)

 在Java1.2版本中，再次改进了安全机制，增加了代码签名。不论本地代码或是远程代码，都会按照用户的安全策略设定，由类加载器加载到虚拟机中权限不同的运行空间，来实现差异化的代码执行权限控制。如下图所示JDK1.2安全模型。

![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\8.png)

 当前最新的安全机制实现，则引入了域(Domain)的概念。虚拟机会把所有代码加载到不同的系统域和应用域，系统域部分专门负责与关键资源进行交互，而各个应用域部分则通过系统域的部分代理来对各种需要的资源进行访问。虚拟机中不同的受保护域(Protected Domain)，对应不一样的权限(Permission)。存在于不同域中的类文件就具有了当前域的全部权限，如下图所示最新的安全模型(jdk 1.6)。

![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\9.png)

**组成沙箱的基本组件：**

- `字节码校验器`(bytecode verifier)︰确保Java类文件遵循lava语言规范。这样可以帮助lava程序实现内存保护。但并不是所有的类文件都会经过字节码校验，比如核心类。

- 类装载器(class loader) :其中类装载器在3个方面对Java沙箱起作用：

  。它防止恶意代码去干涉善意的代码;
  。它守护了被信任的类库边界;
  。它将代码归入保护域，确定了代码可以进行哪些操作。

 虚拟机为不同的类加载器载入的类提供不同的命名空间，命名空间由一系列唯一的名称组成，每一个被装载的类将有一个名字，这个命名空间是由Java虚拟机为每一个类装载器维护的，它们互相之间甚至不可见。

**类装载器采用的机制是双亲委派模式。**

1.从最内层VM自带类加载器开始加载，外层恶意同名类得不到加载从而无法使用；

2.由于严格通过包来区分了访问域，外层恶意的类通过内置代码也无法获得权限访问到内层类，破坏代码就自然无法生效。

- 存取控制器(access controller)︰存取控制器可以控制核心API对操作系统的存取权限，而这个控制的策略设定，可以由用户指定。
- 安全管理器(security manager)︰是核心API和操作系统之间的主要接口。实现权限控制，比存取控制器优先级高。
- 安全软件包(security package) : java.security下的类和扩展包下的类，允许用户为自己的应用增加新的安全特性，包括:
  - 安全提供者
  - 消息摘要
  - 数字签名
  - 加密
  - 鉴别

## 6.Native

- 编写一个多线程类启动。

```java
public class Demo {
    public static void main(String[] args) {
        new Thread(() ->{
        }, "t1").start();
    }
    // 这个Thread是一个类，这个方法定义在这里是不是很诡异！看这个关键字native；
    private native void test();
}
```

- 点进去看start方法的源码：

```java
public synchronized void start() {
      
        if (threadStatus != 0)
            throw new IllegalThreadStateException();
        group.add(this);
        boolean started = false;
        try {
            start0();	// 调用了一个start0方法
            started = true;
        } finally {
            try {
                if (!started) {
                    group.threadStartFailed(this);
                }
            } catch (Throwable ignore) {
              
            }
        }
    }
	// 这个Thread是一个类，这个方法定义在这里是不是很诡异！看这个关键字native；
    private native void start0();
```

在Java编程语言中，“Native”通常指的是与Java虚拟机（JVM）外部的系统进行交互的功能。这些功能主要通过Java Native Interface (JNI) 来实现，允许Java代码调用本地方法或从本地方法调用Java方法。这里的“本地”是指操作系统本身的环境，包括C、C++等编程语言编写的程序。

**Java Native Interface (JNI) 的用途：**

1. 性能优化：对于某些需要大量计算或对性能要求极高的任务，使用本地代码可以比纯Java代码提供更好的执行效率；
2. 访问硬件资源：有些情况下，可能需要直接访问硬件设备（如打印机、摄像头等），而这些操作在Java标准库中没有提供，这时可以通过JNI来实现；
3. 使用现有库：如果已经存在一些用C/C++等语言编写的库，并且希望在Java应用中重用这些库，JNI提供了一种方式来实现这一点；
4. 跨平台开发：虽然Java本身是跨平台的，但在某些特定场景下，可能需要编写针对不同平台的本地代码，以利用特定的操作系统特性。

**使用JNI的步骤：**

- **定义本地方法**：首先在Java类中声明一个或多个本地方法，这些方法前需要加上`native`关键字；
- **加载本地库**：使用`System.loadLibrary()`方法加载包含本地方法实现的动态链接库（DLL，在Windows上）或共享对象文件（.so，在Linux/Unix上）；
- **实现本地方法**：使用C/C++等语言编写本地方法的具体实现，并将其编译成动态链接库；
- **调用本地方法**：在Java应用程序中像调用普通Java方法一样调用这些本地方法。

尽管JNI为Java提供了强大的扩展能力，但它也增加了程序的复杂性和维护难度，因此在决定是否使用JNI时需要权衡利弊。

- 凡是带了native关键字的，说明 java的作用范围达不到，去调用底层C语言的库；
- JNI：Java Native Interface（Java本地方法接口）；
- 凡是带了native关键字的方法就会进入本地方法栈；
- Native Method Stack（本地方法栈）；
- 本地接口的作用是融合不同的编程语言为Java所用，它的初衷是融合C/C++程序，Java在诞生的时候是C/C++横行的时候，想要立足，必须有调用C、C++的程序，于是就在内存中专门开辟了一块区域处理标记为native的代码，它的具体做法是 在 Native Method Stack 中登记native方法，在 ( ExecutionEngine ) 执行引擎执行的时候加载Native Libraies；
- 目前该方法使用的越来越少了，除非是与硬件有关的应用，比如通过Java程序驱动打印机或者Java系统管理生产设备，在企业级应用中已经比较少见。因为现在的异构领域间通信很发达，比如可以使用Socket通信，也可以使用Web Service等等，不多做介绍！

## 7.PC寄存器

程序计数器：Program Counter Register。

每个线程都有一个程序计数器，是线程私有的，就是一个指针，指向方法区中的方法字节码(用来存储指向像一条指令的地址，也即将要执行的指令代码)，在执行引擎读取下一条指令，是一个非常小的内存空间，几乎可以忽略不计。

## 8.方法区

Method Area（方法区）
![在这里插入图片描述](D:\2021\Java\JVM\jvm-study\img\10.png)

- 方法区是被所有线程共享，所有字段和方法字节码，以及一些特殊方法，如构造函数，接口代码也在此定义，简单说，所有定义的方法的信息都保存在该区域，**此区域属于共享区间;**
- 静态变量、常量、类信息(构造方法、接口定义)、运行时的常量池存在方法区中，但是实例变量存在堆内存中，和方法区无关。
- static ，final ，Class ，常量池~

## 9.栈

- 在计算机流传有一句废话： 程序 = 算法 + 数据结构
- 但是对于大部分同学都是： 程序 = 框架 + 业务逻辑
- 栈：先进后出 / 后进先出
- 队列：先进先出（FIFO : First Input First Output）

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621224905303.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

**栈管理程序运行**

- 存储一些基本类型的值、对象的引用、方法等。
- **栈的优势是，存取速度比堆要快，仅次于寄存器，栈数据可以共享。**

思考：为什么main方法最后执行！为什么一个test() 方法执行完了，才会继续走main方法！

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621224919335.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

> **喝多了吐就是栈，吃多了拉就是队列**。

说明：

- 1、栈也叫栈内存，主管Java程序的运行，是在线程创建时创建，它的生命期是跟随线程的生命期，线程结束栈内存也就释放。
- 2、**对于栈来说不存在垃圾回收问题**，只要线程一旦结束，该栈就Over，生命周期和线程一致，是线程私有的。
- 3、方法自己调自己就会导致栈溢出（递归死循环测试）。

**栈里面会放什么东西那？**

- 8大基本类型 + 对象的引用 + 实例的方法

> **栈运行原理**

- Java栈的组成元素——栈帧。
- 栈帧是一种用于帮助虚拟机执行方法调用与方法执行的数据结构。他是独立于线程的，一个线程有自己的一个栈帧。封装了方法的局部变量表、动态链接信息、方法的返回地址以及操作数栈等信息。
- 第一个方法从调用开始到执行完成，就对应着一个栈帧在虚拟机栈中从入栈到出栈的过程。

> 当一个方法A被调用时就产生了一个栈帧F1，并被压入到栈中，A方法又调用了B方法，于是产生了栈帧F2也被压入栈中，B方法又调用了C方法，于是产生栈帧F3也被压入栈中 执行完毕后，先弹出F3， 然后弹出F2，在弹出F1........

- 遵循 “先进后出” / "后进先出" 的原则。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621224937353.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 栈满了，抛出异常：stackOverflowError

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621224952953.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 对象实例化的过程。

## 10.三种JVM

- Sun公司HotSpot java Hotspot™64-Bit server vw (build 25.181-b13，mixed mode)
- BEA JRockit
- IBM 39 VM
- 我们学习都是：Hotspot

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225019127.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

## 11.堆

**Java7**之前

- Heap 堆，一个JVM实例只存在一个堆内存，堆内存的大小是可以调节的。
- 类加载器读取了类文件后，需要把类，方法，常变量放到堆内存中，保存所有引用类型的真实信息，以方便执行器执行。
- 堆内存分为三部分：
  - 新生区 Young Generation Space Young/New
  - 养老区 Tenure generation space Old/Tenure
  - 永久区 Permanent Space Perm
- 堆内存逻辑上分为三部分：新生，养老，永久（元空间 : JDK8 以后名称）。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225034545.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

**谁空谁是to**

- **GC**垃圾回收主要是在新生区和养老区，又分为轻GC 和 重GC，如果内存不够，或者存在死循环，就会导致![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225116754.png#pic_center)
- 在JDK8以后，永久存储区改了个名字(元空间)。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225049387.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

## 12.新生区、养老区

- 新生区是类诞生，成长，消亡的区域，一个类在这里产生，应用，最后被垃圾回收器收集，结束生命。
- 新生区又分为两部分：伊甸区（Eden Space）和幸存者区（Survivor Space），所有的类都是在伊甸区被new出来的，幸存区有两个：0区 和 1区，当伊甸园的空间用完时，程序又需要创建对象，JVM的垃圾回收器将对伊甸园区进行垃圾回收（Minor GC）。将伊甸园中的剩余对象移动到幸存0区，若幸存0区也满了，再对该区进行垃圾回收，然后移动到1区，那如果1区也满了呢？（这里幸存0区和1区是一个互相交替的过程）再移动到养老区，若养老区也满了，那么这个时候将产生MajorGC（Full GC），进行养老区的内存清理，若养老区执行了Full GC后发现依然无法进行对象的保存，就会产生OOM异常 “OutOfMemoryError ”。如果出现 java.lang.OutOfMemoryError：java heap space异常，说明Java虚拟机的堆内存不够，原因如下：
  - 1、Java虚拟机的堆内存设置不够，可以通过参数 -Xms（初始值大小），-Xmx（最大大小）来调整。
  - 2、代码中创建了大量大对象，并且长时间不能被垃圾收集器收集（存在被引用）或者死循环。

## 13.永久区（Perm）

- 永久存储区是一个常驻内存区域，用于存放JDK自身所携带的Class，Interface的元数据，也就是说它存储的是运行环境必须的类信息，被装载进此区域的数据是不会被垃圾回收器回收掉的，关闭JVM才会释放此区域所占用的内存。
- 如果出现 java.lang.OutOfMemoryError：PermGen space，说明是 Java虚拟机对永久代Perm内存设置不够。一般出现这种情况，都是程序启动需要加载大量的第三方jar包，
- 例如：在一个Tomcat下部署了太多的应用。或者大量动态反射生成的类不断被加载，最终导致Perm区被占满。

**注意：**

- JDK1.6之前： 有永久代，常量池1.6在方法区；
- JDK1.7： 有永久代，但是已经逐步 “去永久代”，常量池1.7在堆；
- JDK1.8及之后：无永久代，常量池1.8在元空间。

**熟悉三区结构后方可学习**JVM垃圾回收机制

- 实际而言，方法区（Method Area）和堆一样，是各个线程共享的内存区域，它用于存储虚拟机加载的：类信息+普通常量+静态常量+编译器编译后的代码，虽然JVM规范将方法区描述为**堆的一个逻辑部分，但它却还有一个别名，叫做Non-Heap（非堆），目的就是要和堆分开**。
- 对于HotSpot虚拟机，很多开发者习惯将方法区称之为 “永久代（Parmanent Gen）”，但严格本质上说两者不同，或者说使用永久代实现方法区而已，永久代是方法区（相当于是一个接口interface）的一个实现，Jdk1.7的版本中，已经将原本放在永久代的字符串常量池移走。
- 常量池（Constant Pool）是方法区的一部分，Class文件除了有类的版本，字段，方法，接口描述信息外，还有一项信息就是常量池，这部分内容将在类加载后进入方法区的运行时常量池中存放！

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225139144.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

## 14.堆内存调优

- -Xms：设置初始分配大小，默认为物理内存的 “1/64”。
- -Xmx：最大分配内存，默认为物理内存的 “1/4”。
- -XX:+PrintGCDetails：输出详细的GC处理日志。

> 测试1

**代码测试**

```java
public class Demo01 {
    public static void main(String[] args) {
        // 返回虚拟机试图使用的最大内存
        long max = Runtime.getRuntime().maxMemory();    // 字节：1024*1024
        // 返回jvm的总内存
        long total = Runtime.getRuntime().totalMemory();

        System.out.println("max=" + max + "字节\t" + (max/(double)1024/1024) + "MB");

        System.out.println("total=" + total + "字节\t" + (total/(double)1024/1024) + "MB");

        // 默认情况下:分配的总内存是电脑内存的1/4,初始化的内存是电脑的1/64

    }
}
```

- **IDEA**中进行VM调优参数设置，然后启动。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021062122515494.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225203205.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 发现，默认的情况下分配的内存是总内存的 1/4，而初始化的内存为 1/64 ！

```java
-Xms1024m -Xmx1024m -XX:+PrintGCDetails
```

- VM参数调优：把初始内存，和总内存都调为 1024M，运行，查看结果！

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225214968.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 来大概计算分析一下！

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225224333.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 再次证明：元空间并不在虚拟机中，而是使用本地内存。

> 测试2

代码：

```java
package github.JVM.Demo02;

import java.util.Random;

/**
 * @author subeiLY
 * @create 2021-06-08 10:22
 */
public class Demo02 {
    public static void main(String[] args) {
        String str = "suneiLY";
        while (true) {
            str += str + new Random().nextInt(88888888)
                    + new Random().nextInt(999999999);
        }
    }
}
```

- vm参数：

```java
-Xms8m -Xmx8m -XX:+PrintGCDetails
```

- 测试，查看结果！

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225233536.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 这是一个young 区域撑爆的JAVA 内存日志，其中 PSYoungGen 表示 youngGen分区的变化1536k 表示 GC 之前的大小。
- 488k 表示GC 之后的大小。
- 整个Young区域的大小从 1536K 到 672K , young代的总大小为 7680K。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225244781.png#pic_center)

- user – 总计本次 GC 总线程所占用的总 CPU 时间。
- sys – OS 调用 or 等待系统时间。
- real – 应用暂停时间。
- 如果GC 线程是 Serial Garbage Collector 串行搜集器的方式的话（只有一条GC线程,）， real time 等于user 和 system 时间之和。
- 通过日志发现Young的区域到最后 GC 之前后都是0，old 区域 无法释放，最后报堆溢出错误。

**其他文章链接**

- [一文读懂 - 元空间和永久代](https://juejin.cn/post/684490402096480257)
- [Java方法区、永久代、元空间、常量池详解](https://blog.csdn.net/u011635492/article/details/81046174?utm_medium=distribute.pc_relevant.none-task-blog-2~default~BlogCommendFromMachineLearnPai2~default-2.control&dist_request_id=1331647.219.16183160373688617&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~BlogCommendFromMachineLearnPai2~default-2.control)

## 15.GC

### 1.Dump内存快照

 在运行java程序的时候，有时候想测试运行时占用内存情况，这时候就需要使用测试工具查看了。在eclipse里面有 **Eclipse Memory Analyzer tool(MAT)**插件可以测试，而在idea中也有这么一个插件，就是**JProfiler**，一款性能瓶颈分析工具！

**作用**：

- 分析Dump文件，快速定位内存泄漏；
- 获得堆中对象的统计数据
- 获得对象相互引用的关系
- 采用树形展现对象间相互引用的情况

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225310810.png#pic_center)

> 安装JProﬁler

1. IDEA插件安装

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225332813.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

1. 安装JProﬁler监控软件

- 下载地址：[https://www.ej-technologies.com/download/jproﬁler/version_92](https://www.ej-technologies.com/download/jprofiler/version_92)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225348141.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

1. 下载完双击运行，选择自定义目录安装，点击Next。

- 注意：安装路径，**建议选择一个文件名中没有中文，没有空格的路径** ，否则识别不了。然后一直点Next。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225401368.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

1. 注册

```java
// 注册码仅供大家参考
L-Larry_Lau@163.com#23874-hrwpdp1sh1wrn#0620
L-Larry_Lau@163.com#36573-fdkscp15axjj6#25257
L-Larry_Lau@163.com#5481-ucjn4a16rvd98#6038
L-Larry_Lau@163.com#99016-hli5ay1ylizjj#27215
L-Larry_Lau@163.com#40775-3wle0g1uin5c1#0674
```

1. 配置IDEA运行环境

- Settings–Tools–JProﬂier–JProﬂier executable选择JProﬁle安装可执行文件。（如果系统只装了一个版本， 启动IDEA时会默认选择）保存。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225412283.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 代码测试：

```java
package github.JVM.Demo02;

import java.util.ArrayList;

/**
 * @author subeiLY
 * @create 2021-06-08 11:13
 */
public class Demo03 {
    byte[] byteArray = new byte[1*1024*1024]; // 1M = 1024K


    public static void main(String[] args) {
        ArrayList<Demo03> list = new ArrayList<>();
        int count = 0;
        try {
            while (true) {
                list.add(new Demo03());  // 问题所在
                count = count + 1;
            }
        } catch (Error e) {
            System.out.println("count:" + count);
            e.printStackTrace();
        }
    }
}
```

- vm参数 ： `-Xms1m -Xmx8m -XX:+HeapDumpOnOutOfMemoryError`

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225421179.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 寻找文件：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225442999.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

> 使用 Jproﬁler 工具分析查看

双击这个文件默认使用 Jproﬁler 进行 Open大的对象！

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225452677.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225503142.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225512661.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 从软件开发的角度上，dump文件就是当程序产生异常时，用来记录当时的程序状态信息（例如堆栈的状态），用于程序开发定位问题。

### 2.GC四大算法

#### 1.引用计数法

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225533583.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 每个对象有一个引用计数器，当对象被引用一次则计数器加1，当对象引用失效一次，则计数器减1，对于计数器为0的对象意味着是垃圾对象，可以被GC回收。
- 目前虚拟机基本都是采用可达性算法，从GC Roots 作为起点开始搜索，那么整个连通图中的对象边都是活对象，对于GC Roots 无法到达的对象变成了垃圾回收对象，随时可被GC回收。

#### 2.复制算法

- 年轻代中使用的是Minor GC，采用的就是复制算法（Copying）。

**什么是复制算法？**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225552897.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- Minor GC 会把Eden中的所有活的对象都移到Survivor区域中，如果Survivor区中放不下，那么剩下的活的对象就被移动到Old generation中，**也就是说，一旦收集后，Eden就是变成空的了**
- 当对象在Eden（包括一个Survivor区域，这里假设是From区域）出生后，在经过一次Minor GC后，如果对象还存活，并且能够被另外一块Survivor区域所容纳 （上面已经假设为from区域，这里应为to区域，即to区域有足够的内存空间来存储Eden 和 From 区域中存活的对象），则使用**复制算法**将这些仍然还活着的对象复制到另外一块Survivor区域（即 to 区域）中，然后清理所使用过的Eden 以及Survivor 区域（即form区域），并且将这些对象的年龄设置为1，以后对象在Survivor区，每熬过一次MinorGC，就将这个对象的年龄 + 1，当这个对象的年龄达到某一个值的时候（默认是15岁，通过- XX:MaxTenuringThreshold 设定参数）这些对象就会成为老年代。
- `-XX:MaxTenuringThreshold` 任期门槛=>设置对象在新生代中存活的次数

> 面试题：如何判断哪个是to区呢？一句话：**谁空谁是to**

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225611642.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

**原理解释：**

- 年轻代中的GC，主要是复制算法（Copying）
- HotSpot JVM 把年轻代分为了三部分：一个 Eden 区 和 2 个Survivor区（from区 和 to区）。默认比例为 8:1:1，一般情况下，新创建的对象都会被分配到Eden区（一些大对象特殊处理），这些对象经过第一次Minor GC后，如果仍然存活，将会被移到Survivor区，对象在Survivor中每熬过一次Minor GC ， 年龄就会增加1岁，当它的年龄增加到一定程度时，就会被移动到年老代中，因为年轻代中的对象基本上 都是朝生夕死，所以在年轻代的垃圾回收算法使用的是复制算法！复制算法的思想就是将内存分为两块，每次只用其中一块，当这一块内存用完，就将还活着的对象复制到另外一块上面。复制算法不会产 生内存碎片！

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225624904.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 在GC开始的时候，对象只会在Eden区和名为 “From” 的Survivor区，Survivor区“TO” 是空的，紧接着进行GC，Eden区中所有存活的对象都会被复制到 “To”，而在 “From” 区中，仍存活的对象会更具他们的年龄值来决定去向。
- 年龄达到一定值的对象会被移动到老年代中，没有达到阈值的对象会被复制到 “To 区域”，经过这次GC后，Eden区和From区已经被清空，这个时候， “From” 和 “To” 会交换他们的角色， 也就是新的 “To” 就是GC前的“From” ， 新的 “From” 就是上次GC前的 “To”。
- 不管怎样，都会保证名为To 的Survicor区域是空的。 Minor GC会一直重复这样的过程。直到 To 区 被填满 ，“To” 区被填满之后，会将所有的对象移动到老年代中。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225635376.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 因为Eden区对象一般存活率较低，一般的，使用两块10%的内存作为空闲和活动区域，而另外80%的内存，则是用来给新建对象分配内存的。一旦发生GC，将10%的from活动区间与另外80%中存活的Eden 对象转移到10%的to空闲区域，接下来，将之前的90%的内存，全部释放，以此类推；
- 好处：没有内存碎片；坏处：浪费内存空间。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225650554.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

**劣势：**

- 复制算法它的缺点也是相当明显的。
  - 1、他浪费了一半的内存，这太要命了。
  - 2、如果对象的存活率很高，我们可以极端一点，假设是100%存活，那么我们需要将所有对象都复制一遍，并将所有引用地址重置一遍。复制这一工作所花费的时间，在对象存活率达到一定程度时，将会变的不可忽视，所以从以上描述不难看出。复制算法要想使用，最起码对象的存活率要非常低才行，而且 最重要的是，我们必须要克服50%的内存浪费。

> 标记清除（Mark-Sweep）

- 回收时，对需要存活的对象进行标记；
- 回收不是绿色的对象。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225706360.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 当堆中的有效内存空间被耗尽的时候，就会停止整个程序（也被称为stop the world），然后进行两项工作，第一项则是标记，第二项则是清除。
- 标记：从引用根节点开始标记所有被引用的对象，标记的过程其实就是遍历所有的GC Roots ，然后将所有GC Roots 可达的对象，标记为存活的对象。
- 清除： 遍历整个堆，把未标记的对象清除。
- 缺点：这个算法需要暂停整个应用，会产生内存碎片。两次扫描，严重浪费时间。

> 用通俗的话解释一下 标记/清除算法，就是当程序运行期间，若可以使用的内存被耗尽的时候，GC线程就会被触发并将程序暂停，随后将依旧存活的对象标记一遍，最终再将堆中所有没被标记的对象全部清 除掉，接下来便让程序恢复运行。

**劣势：**

1. 首先、它的缺点就是效率比较低（递归与全堆对象遍历），而且在进行GC的时候，需要停止应用 程序，这会导致用户体验非常差劲
2. 其次、主要的缺点则是这种方式清理出来的空闲内存是不连续的，这点不难理解，我们的死亡对象 都是随机的出现在内存的各个角落，现在把他们清除之后，内存的布局自然乱七八糟，而为了应付 这一点，JVM就不得不维持一个内存空间的空闲列表，这又是一种开销。而且在分配数组对象的时 候，寻找连续的内存空间会不太好找。

#### 3.标记压缩

- 标记整理说明：老年代一般是由标记清除或者是标记清除与标记整理的混合实现。

**什么是标记压缩？**

**原理：**

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021062122575235.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225801551.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 在整理压缩阶段，不再对标记的对象作回收，而是通过所有存活对象都像一端移动，然后直接清除边界以外的内存。可以看到，标记的存活对象将会被整理，按照内存地址依次排列，而未被标记的内存会被 清理掉，如此一来，当我们需要给新对象分配内存时，JVM只需要持有一个内存的起始地址即可，这比维护一个空闲列表显然少了许多开销。
- 标记、整理算法 不仅可以弥补 标记、清除算法当中，内存区域分散的缺点，也消除了复制算法当中，内存减半的高额代价；

#### 4.标记清除压缩

- 先标记清除几次，再压缩。

![在这里插入图片描述](https://img-blog.csdnimg.cn/2021062122581315.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

### 3.总结

- 内存效率：复制算法 > 标记清除算法 > 标记压缩算法 （时间复杂度）；
- 内存整齐度：复制算法 = 标记压缩算法 > 标记清除算法；
- 内存利用率：标记压缩算法 = 标记清除算法 > 复制算法；

 可以看出，效率上来说，复制算法是当之无愧的老大，但是却浪费了太多内存，而为了尽量兼顾上面所 提到的三个指标，标记压缩算法相对来说更平滑一些 ， 但是效率上依然不尽如人意，它比复制算法多了一个标记的阶段，又比标记清除多了一个整理内存的过程。

> 难道就没有一种最优算法吗？
>
> 答案： 无，没有最好的算法，只有最合适的算法 。 -----------> 分代收集算法

**年轻代：**（Young Gen）

- 年轻代特点是区域相对老年代较小，对象存活低。
- 这种情况复制算法的回收整理，速度是最快的。复制算法的效率只和当前存活对象大小有关，因而很适 用于年轻代的回收。而复制算法内存利用率不高的问题，通过hotspot中的两个survivor的设计得到缓解。

**老年代：**（Tenure Gen）

- 老年代的特点是区域较大，对象存活率高！
- 这种情况，存在大量存活率高的对象，复制算法明显变得不合适。一般是由标记清除或者是标记清除与标记整理的混合实现。Mark阶段的开销与存活对象的数量成正比，这点来说，对于老年代，标记清除或 者标记整理有一些不符，但可以通过多核多线程利用，对并发，并行的形式提标记效率。Sweep阶段的 开销与所管理里区域的大小相关，但Sweep “就地处决” 的 特点，回收的过程没有对象的移动。使其相对其他有对象移动步骤的回收算法，仍然是是效率最好的，但是需要解决内存碎片的问题。

## 16.JMM

1. 什么是JMM？
   - JMM：（Java Memory Model的缩写）（Java内存模型）
2. 他干嘛的？官方，其他人的博客，对应的视频！
   - 作用：缓存一致性协议，用于定义数据读写的规则(遵守，找到这个规则)。
   - JMM定义了线程工作内存和主内存之间的抽象关系∶线程之间的共享变量存储在主内存(Main Memory)中，每个线程都有一个私有的本地内存（Local Memory)。

![在这里插入图片描述](https://img-blog.csdnimg.cn/20210621225826231.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L20wXzQ2MTUzOTQ5,size_16,color_FFFFFF,t_70#pic_center)

- 解决共享对象可见性这个问题：volilate

1. 它该如何学习？
   - JMM：抽象的概念，理论。

- JMM对这八种指令的使用

  ，制定了如下规则：

  - 不允许read和load、store和write操作之一单独出现。即使用了read必须load，使用了store必须write。
  - 不允许线程丢弃他最近的assign操作，即工作变量的数据改变了之后，必须告知主存。
  - 不允许一个线程将没有assign的数据从工作内存同步回主内存。
  - 一个新的变量必须在主内存中诞生，不允许工作内存直接使用一个未被初始化的变量。就是怼变量实施use、store操作之前，必须经过assign和load操作。
  - 一个变量同一时间只有一个线程能对其进行lock。多次lock后，必须执行相同次数的unlock才能解锁。
  - 如果对一个变量进行lock操作，会清空所有工作内存中此变量的值，在执行引擎使用这个变量前，必须重新load或assign操作初始化变量的值。
  - 如果一个变量没有被lock，就不能对其进行unlock操作。也不能unlock一个被其他线程锁住的变量。
  - 对一个变量进行unlock操作之前，必须把此变量同步回主内存。

　　JMM对这八种操作规则和对[volatile的一些特殊规则](https://www.cnblogs.com/null-qige/p/8569131.html)就能确定哪里操作是线程安全，哪些操作是线程不安全的了。但是这些规则实在复杂，很难在实践中直接分析。所以一般我们也不会通过上述规则进行分析。更多的时候，使用java的happen-before规则来进行分析。

欢迎查阅

本文作者：subeiLY

本文链接：https://www.cnblogs.com/gh110/p/14917326.html

版权声明：本作品采用知识共享署名-非商业性使用-禁止演绎 3.0 中国大陆许可协议进行许可。[许可协议](https://creativecommons.org/licenses/by-nc-sa/3.0/cn/legalcode)进行许可。

[好文要顶](javascript:void(0);)[关注我](javascript:void(0);)[收藏该文](javascript:void(0);)打赏[微信分享](javascript:void(0);)



## 99P06 9.栈

# JVM 面试题

## 1、请你谈谈你对JVM的理解?

Java虚拟机（Java Virtual Machine, JVM）是Java平台的核心组件之一，它是一个抽象的计算机，专为执行Java字节码而设计。JVM具有以下主要特点和功能：
1. **平台无关性**：
   - Java程序编译后会生成一种称为“字节码”（.class文件）的中间语言，这种字节码可以在任何实现了JVM的平台上运行，这正是“一次编写，到处运行”（Write Once, Run Anywhere）理念的基础。

2. **自动内存管理**：
   - JVM提供了一种自动化的内存管理系统，包括垃圾回收机制。程序员不需要手动释放不再使用的对象所占用的内存，JVM会自动追踪并回收这些内存，从而减少了内存泄漏的风险。

3. **安全性**：
   - JVM设计时考虑了安全性，能够防止恶意代码对本地系统造成损害。例如，通过沙箱环境限制代码访问权限，以及类型安全检查等措施。

4. **性能优化**：
   - JVM通过即时编译（Just-In-Time Compilation, JIT）、热点代码优化等技术提高程序执行效率。JIT编译器可以将频繁执行的字节码转换成机器码，以加速执行速度。

5. **多线程支持**：
   - Java语言本身支持多线程编程，而JVM提供了底层实现，使得多线程操作更加高效和安全。

6. **类加载机制**：
   - JVM有一套复杂的类加载机制，它负责在程序运行期间动态地加载所需的类文件。这个过程涉及到类的验证、准备、解析和初始化等多个步骤。

7. **异常处理**：
   - JVM定义了一套标准的异常处理框架，允许开发者捕获和处理运行时错误，提高了程序的健壮性和用户体验。

JVM不仅仅局限于支持Java语言，许多其他语言如Scala、Kotlin、Clojure等也都是基于JVM来运行的。这得益于JVM提供的强大而灵活的执行环境，使其成为了跨语言开发的重要平台。随着技术的发展，JVM也在不断地进行优化和改进，以适应更多新的应用场景和技术需求。

## 2、java21虚拟机和之前的变化更新?

Java 21 是 Java 发展过程中的一个重要版本，它不仅延续了之前版本的改进趋势，还引入了一些令人瞩目的新特性。以下是一些 Java 21 虚拟机（JVM）及其相关平台的主要变化和更新：

1. 虚拟线程（Virtual Threads）

- **简介**：虚拟线程是 Java 21 中最引人注目的特性之一。它们是一种轻量级的线程实现，旨在降低并发编程的复杂性和开销。虚拟线程允许开发者创建成千上万个线程而不会耗尽系统资源。
- **优势**：虚拟线程可以显著提高应用程序的并发性能，尤其是在处理大量 I/O 操作或网络请求时。它们通过减少线程上下文切换的开销，提高了系统的响应速度和吞吐量。
- **实现**：虚拟线程在 JVM 层面进行了优化，使用用户模式线程（user-mode threads）或纤程（fibers）来实现，而不是直接映射到操作系统线程。

2. 字符串模板（String Templates）

- **JEP 430**：字符串模板是 Java 21 中的一个预览特性，它允许在字符串中嵌入表达式，类似于其他编程语言中的字符串插值。

- **语法**：使用 `\(expression)` 语法在字符串中插入表达式的结果。

- 示例

  ```java
  String name = "Alice";
  int age = 30;
  String message = "\(name) is \(age) years old.";
  ```

3. ZGC 分代收集

- **简介**：ZGC（Z Garbage Collector）是一个低延迟的垃圾收集器，最初在 Java 11 中引入。Java 21 中，ZGC 进一步发展，支持分代收集。
- **优势**：分代收集根据对象的年龄将堆划分为多个区域，从而更有效地管理内存，减少垃圾收集的停顿时间。
- **配置**：可以通过 `-XX:+UseZGC` 和 `-XX:+ZGenerational` 选项启用 ZGC 分代收集。

4. 新的 API 和语言特性

- **记录类（Records）**：记录类在 Java 14 中作为预览特性引入，Java 16 中成为正式特性。Java 21 继续对其进行了改进，使其更加成熟和稳定。
- **模式匹配 for instanceof**：在 Java 16 中引入，Java 21 中进一步完善，使 `instanceof` 检查更加简洁和安全。
- **密封类（Sealed Classes）**：在 Java 17 中引入，Java 21 中继续改进，提供更强的封装和控制能力。

5. 性能和安全性的提升

- **性能优化**：Java 21 对 JVM 的性能进行了多项优化，包括垃圾收集器的改进、JIT 编译器的优化等。
- **安全性增强**：引入了新的安全特性，如更严格的模块化系统、更强大的加密算法支持等。

6. 其他改进

- **模块化系统**：Java 9 引入的模块化系统在 Java 21 中继续得到改进，提供了更灵活的模块管理和依赖管理。
- **JFR（Java Flight Recorder）**：JFR 是一个强大的性能分析工具，Java 21 中对其进行了增强，提供了更多的诊断和监控功能。

总结

Java 21 通过引入虚拟线程、字符串模板、ZGC 分代收集等重要特性，显著提升了 Java 平台的并发性能、开发效率和安全性。这些改进不仅使 Java 保持了其在企业级应用中的领先地位，还为开发者提供了更多的工具和灵活性来构建高性能、可维护的应用程序。

## 3、什么是OOM，什么是栈溢出StackOverFlowError? 怎么分析?

什么是 OOM (Out of Memory Error)

Out of Memory Error (OOM)是 Java 虚拟机（JVM）在运行时无法分配足够的内存来完成某个操作时抛出的一种错误。这种错误通常发生在以下几种情况：

1. **堆内存不足** (`java.lang.OutOfMemoryError: Java heap space`)：
   - 当 JVM 试图在堆上分配对象，但没有足够的可用内存，且垃圾收集器也无法释放更多内存时，会抛出此错误。
   - 解决方法：增加堆内存大小（使用 `-Xmx` 参数），优化代码减少内存占用，检查是否有内存泄漏。

2. **永久代/元空间不足** (`java.lang.OutOfMemoryError: PermGen space` 或 `java.lang.OutOfMemoryError: Metaspace`)：
   - 在 Java 8 及之前，当永久代（PermGen）空间不足时会抛出此错误。永久代用于存储类的元数据。
   - 在 Java 8 及之后，永久代被元空间（Metaspace）取代，元空间位于本地内存中。
   - 解决方法：增加永久代或元空间的大小（使用 `-XX:MaxPermSize` 或 `-XX:MaxMetaspaceSize` 参数），优化类加载机制，避免不必要的类加载。

3. **直接内存不足** (`java.lang.OutOfMemoryError: Direct buffer memory`)：
   - 当使用 `ByteBuffer.allocateDirect` 分配直接缓冲区时，如果直接内存不足，会抛出此错误。
   - 解决方法：增加直接内存的大小（使用 `-XX:MaxDirectMemorySize` 参数），优化直接缓冲区的使用。

什么是 StackOverflowError

StackOverflowError是 Java 虚拟机在执行线程时，由于栈深度超过限制而抛出的一种错误。这种错误通常发生在以下几种情况：

1. **递归调用过深**：
   - 当方法调用自身或调用链过长，导致栈帧数量超过栈的最大容量时，会抛出此错误。
   - 解决方法：优化递归算法，使用迭代代替递归，或者增加栈的大小（使用 `-Xss` 参数）。

2. **局部变量过多**：
   - 当方法中定义了大量局部变量，导致单个栈帧过大，超出栈的最大容量时，也会抛出此错误。
   - 解决方法：减少方法中的局部变量数量，优化方法的设计。

如何分析 OOM 和 StackOverflowError

分析 OOM

1. **启用堆转储**：
   - 使用 `-XX:+HeapDumpOnOutOfMemoryError` 参数，当发生 OOM 时，JVM 会自动生成堆转储文件（heap dump）。
   - 使用 `-XX:HeapDumpPath=<path>` 指定堆转储文件的保存路径。

2. **使用内存分析工具**：
   - **VisualVM**：一个集成了多种监控、分析和故障排除工具的图形界面工具。
   - **Eclipse MAT (Memory Analyzer Tool)**：一个强大的堆转储分析工具，可以帮助识别内存泄漏和大对象。
   - **JProfiler** 或 **YourKit**：商业的性能分析工具，提供更详细的内存分析功能。

3. **查看日志**：
   - 查看应用程序的日志文件，寻找与内存使用相关的警告或错误信息。
   - 使用 `-XX:+PrintGCDetails` 和 `-XX:+PrintGCTimeStamps` 参数，输出详细的垃圾收集日志，帮助分析内存使用情况。

分析 StackOverflowError

1. **查看堆栈跟踪**：
   - 当发生 `StackOverflowError` 时，JVM 会打印出堆栈跟踪信息，显示导致错误的方法调用链。
   - 仔细检查堆栈跟踪，找出递归调用的源头或方法中局部变量过多的问题。

2. **使用调试工具**：
   - 使用 IDE 的调试功能，逐步执行代码，观察方法调用的深度和局部变量的使用情况。
   - 设置断点，检查递归调用的条件，确保递归能够正确终止。

3. **增加栈大小**：
   - 使用 `-Xss` 参数增加线程栈的大小，例如 `-Xss512k`。
   - 注意，增加栈大小可能会消耗更多的内存，需要权衡性能和资源使用。

通过以上方法，可以有效地分析和解决 OOM 和 `StackOverflowError`，提高应用程序的稳定性和性能。

## 4、JVM的常用调优参数有哪些?

JVM 调优参数可以帮助优化应用程序的性能，特别是在处理高负载和大数据量的情况下。以下是一些常用的 JVM 调优参数及其用途：

1. 堆内存设置

- **-Xms**：设置初始堆内存大小。
  
  示例：`-Xms512m` 表示初始堆内存为 512MB。
  
- **-Xmx**：设置最大堆内存大小。
  
  示例：`-Xmx2g` 表示最大堆内存为 2GB。

2. 永久代/元空间设置

- **-XX:PermSize**（Java 8 之前）：设置初始永久代大小。
  
  示例：`-XX:PermSize=128m` 表示初始永久代大小为 128MB。
  
- **-XX:MaxPermSize**（Java 8 之前）：设置最大永久代大小。
  
  示例：`-XX:MaxPermSize=256m` 表示最大永久代大小为 256MB。
  
- **-XX:MetaspaceSize**（Java 8 及之后）：设置初始元空间大小。
  
  示例：`-XX:MetaspaceSize=128m` 表示初始元空间大小为 128MB。
  
- **-XX:MaxMetaspaceSize**（Java 8 及之后）：设置最大元空间大小。
  
  示例：`-XX:MaxMetaspaceSize=256m` 表示最大元空间大小为 256MB。

3. 垃圾收集器设置

- **-XX:+UseSerialGC**：使用串行垃圾收集器。
  
- **-XX:+UseParallelGC**：使用并行垃圾收集器。
  - **-XX:ParallelGCThreads**：设置并行垃圾收集器的线程数。
    
    示例：`-XX:ParallelGCThreads=4` 表示使用 4 个线程进行垃圾收集。
  
- **-XX:+UseConcMarkSweepGC**：使用 CMS（Concurrent Mark Sweep）垃圾收集器。
  - **-XX:CMSInitiatingOccupancyFraction**：设置触发 CMS 收集的堆内存占用率。
    
    示例：`-XX:CMSInitiatingOccupancyFraction=70` 表示当堆内存占用率达到 70% 时触发 CMS 收集。
  
- **-XX:+UseG1GC**：使用 G1（Garbage First）垃圾收集器。
  - **-XX:MaxGCPauseMillis**：设置最大垃圾收集暂停时间目标。
    
    示例：`-XX:MaxGCPauseMillis=200` 表示目标最大暂停时间为 200 毫秒。
  - **-XX:InitiatingHeapOccupancyPercent**：设置触发 G1 收集的堆内存占用率。
    
    示例：`-XX:InitiatingHeapOccupancyPercent=45` 表示当堆内存占用率达到 45% 时触发 G1 收集。
  
- **-XX:+UseZGC**：使用 ZGC（Z Garbage Collector）垃圾收集器。
  - **-XX:SoftMaxHeapSize**：设置软最大堆内存大小。
  
    示例：`-XX:SoftMaxHeapSize=4g` 表示软最大堆内存为 4GB。

4. 堆栈设置

- **-Xss**：设置每个线程的栈大小。

  示例：`-Xss512k` 表示每个线程的栈大小为 512KB。

5. 垃圾收集日志

- **-XX:+PrintGCDetails**：打印详细的垃圾收集日志。
- **-XX:+PrintGCDateStamps**：在垃圾收集日志中包含时间戳。
- **-Xloggc:<file>**：指定垃圾收集日志文件的路径。
  
  示例：`-Xloggc:/var/log/gc.log`

6. JIT 编译器设置

- **-XX:+TieredCompilation**：启用分层编译，提高启动性能。
- **-XX:CompileThreshold**：设置方法被 JIT 编译的调用次数阈值。
  
  示例：`-XX:CompileThreshold=1000` 表示方法被调用 1000 次后进行 JIT 编译。

7. 直接内存设置

- **-XX:MaxDirectMemorySize**：设置最大直接内存大小。
  
  示例：`-XX:MaxDirectMemorySize=256m` 表示最大直接内存为 256MB。

8. 其他常用参数

- **-Dfile.encoding=UTF-8**：设置文件编码为 UTF-8。
- **-Djava.net.preferIPv4Stack=true**：优先使用 IPv4 地址。
- **-XX:+HeapDumpOnOutOfMemoryError**：在发生 OutOfMemoryError 时生成堆转储文件。
- **-XX:HeapDumpPath=<path>**：指定堆转储文件的保存路径。
  
  示例：`-XX:HeapDumpPath=/var/log/heapdump.hprof`

示例

以下是一个综合的 JVM 调优参数示例：

```sh
java -Xms2g -Xmx4g -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=45 -Xss512k -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/var/log/gc.log -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/heapdump.hprof -jar myapp.jar
```

通过合理设置这些参数，可以显著提高应用程序的性能和稳定性。不同的应用场景可能需要不同的调优策略，因此建议在实际环境中进行测试和调整。

## 5、内存快照如何抓取？怎么分析Dump文件？

抓取内存快照（Heap Dump）并在其中进行分析是诊断和解决 Java 应用程序内存问题的重要手段。以下是抓取内存快照和分析 Dump 文件的详细步骤：

抓取内存快照

1. 使用 JVM 参数自动捕获

你可以在启动 JVM 时使用以下参数，当发生 `OutOfMemoryError` 时自动生成堆转储文件：

```sh
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/to/dumpfile.hprof
```

2. 使用 jmap 工具手动捕获

`jmap` 是 JDK 自带的工具，可以用来生成堆转储文件。以下是使用 `jmap` 的步骤：

1. 找到 Java 进程的 PID：
   ```sh
   jps
   ```
   或者使用 `ps` 命令：
   ```sh
   ps -ef | grep java
   ```

2. 生成堆转储文件：
   ```sh
   jmap -dump:live,format=b,file=/path/to/dumpfile.hprof <PID>
   ```
   - `live`：只导出活动对象。
   - `format=b`：表示二进制格式。
   - `file=/path/to/dumpfile.hprof`：指定生成的堆转储文件的路径和名称。
   - `<PID>`：Java 进程的进程 ID。

3. 使用 VisualVM 工具捕获

VisualVM 是一个图形化的工具，可以用来监控和分析 Java 应用程序。以下是使用 VisualVM 的步骤：

1. **启动 VisualVM**：
   ```sh
   visualvm
   ```

2. **连接到目标 Java 进程**：
   在 VisualVM 主界面中，选择你要分析的 Java 进程。

3. **生成堆转储文件**：
   在选定的进程上右键点击，选择 `Heap Dump` 选项。生成的堆转储文件会自动打开在 VisualVM 中。

分析 Dump 文件

1. 使用 VisualVM 分析

VisualVM 是一个非常强大的工具，可以直接在其中分析堆转储文件。

1. **打开堆转储文件**：
   在 VisualVM 中，选择 `File` -> `Load`，然后选择你生成的堆转储文件。

2. **查看对象列表**：
   在堆转储视图中，可以看到所有对象的列表。可以按类名、实例数等进行排序，查找占用内存较多的对象。

3. **查看对象的引用关系**：
   选择一个对象，可以查看它的引用关系，了解为什么这个对象没有被垃圾回收。

4. **使用 OQL（Object Query Language）**：
   VisualVM 提供了一个 OQL 查询语言，可以用来编写复杂的查询，查找特定的对象或对象集合。

2. 使用 Eclipse Memory Analyzer (MAT) 分析

Eclipse Memory Analyzer (MAT) 是一个专门用于分析堆转储文件的工具，功能非常强大。

1. **下载并安装 MAT**：
   你可以从 [Eclipse 官方网站]下载 MAT。

2. **打开堆转储文件**：
   启动 MAT，选择 `File` -> `Open Heap Dump`，然后选择你生成的堆转储文件。

3. **使用报告**：
   MAT 会生成一些预定义的报告，如 `Leak Suspects` 报告，帮助你快速定位潜在的内存泄漏问题。

4. **查看对象列表**：
   在 `Dominator Tree` 视图中，可以看到所有对象的层次结构，按占用内存大小排序。

5. **查看对象的引用关系**：
   选择一个对象，可以查看它的引用关系，了解为什么这个对象没有被垃圾回收。

6. **使用 OQL**：
   MAT 也支持 OQL 查询，可以编写复杂的查询来查找特定的对象或对象集合。

示例分析步骤

假设你已经生成了一个堆转储文件 `heapdump.hprof`，以下是使用 MAT 进行分析的步骤：

1. **打开堆转储文件**：
   启动 MAT，选择 `File` -> `Open Heap Dump`，然后选择 `heapdump.hprof`。

2. **查看 `Leak Suspects` 报告**：
   在 MAT 中，选择 `Leak Suspects` 报告，查看 MAT 自动生成的内存泄漏嫌疑对象。

3. **查看 `Dominator Tree`**：
   选择 `Dominator Tree` 视图，查看按占用内存大小排序的对象列表。

4. **查找大对象**：
   在 `Dominator Tree` 中，查找占用内存较大的对象，分析它们的引用关系，确定是否有必要优化。

5. **使用 OQL 查询**：
   如果需要查找特定的对象或对象集合，可以使用 OQL 查询。例如，查找所有 `String` 对象：
   ```sql
   SELECT * FROM java.lang.String
   ```

通过这些步骤，你可以有效地分析堆转储文件，找出内存泄漏的原因，优化应用程序的内存使用。

## 6、谈谈JVM中，类加载器你的认识？

在 Java 虚拟机（JVM）中，类加载器（Class Loader）是一个非常重要的组件，负责将类文件从文件系统、网络或其他来源加载到内存中，并转换为 `java.lang.Class` 实例。类加载器在 Java 的类加载机制中起着核心作用，确保类的加载、链接和初始化过程顺利进行。以下是对 JVM 中类加载器的详细介绍：

类加载器的基本概念

1. **类加载过程**：
   - **加载（Loading）**：将类的二进制数据从文件系统、网络或其他来源读取到内存中，并转换为 `java.lang.Class` 实例。
   - **验证（Verification）**：确保加载的类文件符合 JVM 规范，没有安全问题。
   - **准备（Preparation）**：为类的静态变量分配内存，并设置默认初始值。
   - **解析（Resolution）**：将类中的符号引用转换为直接引用。
   - **初始化（Initialization）**：执行类的初始化代码，包括静态初始化块和静态变量的赋值。

2. **类加载器的层次结构**：
   - **启动类加载器（Bootstrap Class Loader）**：由 JVM 实现提供的原生类加载器，负责加载核心类库（如 `rt.jar` 中的类）。
   - **扩展类加载器（Extension Class Loader）**：负责加载 Java 扩展目录（如 `jre/lib/ext`）中的类。
   - **应用程序类加载器（Application Class Loader）**：也称为系统类加载器，负责加载应用程序类路径（如 `CLASSPATH`）中的类。
   - **自定义类加载器**：开发者可以根据需要创建自定义类加载器，以实现特定的类加载逻辑。

类加载器的工作原理

1. **双亲委派模型**：
   - **定义**：双亲委派模型是一种类加载器之间的委托机制。当一个类加载器收到类加载请求时，它首先将请求委派给父类加载器，只有当父类加载器无法加载该类时，才会尝试自己加载。
   - **优点**：确保了类的唯一性和安全性。例如，防止用户自定义的 `java.lang.Object` 类覆盖标准库中的 `java.lang.Object` 类。
   - **实现**：每个类加载器都有一个父类加载器，根类加载器（Bootstrap Class Loader）没有父类加载器。

2. **类加载器的委托顺序**：
   - 当应用程序类加载器收到类加载请求时，它会先委托给扩展类加载器。
   - 扩展类加载器再委托给启动类加载器。
   - 如果启动类加载器无法加载该类，则返回给扩展类加载器。
   - 如果扩展类加载器也无法加载该类，则返回给应用程序类加载器。
   - 最后，应用程序类加载器尝试加载该类。

自定义类加载器

1. **创建自定义类加载器**：
   - 继承 `java.lang.ClassLoader` 类。
   - 重写 `findClass` 方法，实现自定义的类加载逻辑。
   - 调用 `defineClass` 方法将字节数组转换为 `java.lang.Class` 实例。

2. **示例**：
   ```java
   public class MyClassLoader extends ClassLoader {
       private String classPath;
   
       public MyClassLoader(String classPath) {
           this.classPath = classPath;
       }
   
       @Override
       protected Class<?> findClass(String name) throws ClassNotFoundException {
           byte[] classData = loadClassData(name);
           if (classData == null) {
               throw new ClassNotFoundException();
           } else {
               return defineClass(name, classData, 0, classData.length);
           }
       }
   
       private byte[] loadClassData(String className) {
           String path = classPath + File.separatorChar + className.replace('.', File.separatorChar) + ".class";
           try (InputStream is = new FileInputStream(path);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
               int bufferSize = 1024;
               byte[] buffer = new byte[bufferSize];
               int len = 0;
               while ((len = is.read(buffer)) != -1) {
                   baos.write(buffer, 0, len);
               }
               return baos.toByteArray();
           } catch (IOException e) {
               e.printStackTrace();
               return null;
           }
       }
   }
   ```

3. **使用自定义类加载器**：
   ```java
   public class TestCustomClassLoader {
       public static void main(String[] args) {
           MyClassLoader myClassLoader = new MyClassLoader("path/to/classes");
           try {
               Class<?> clazz = myClassLoader.loadClass("com.example.MyClass");
               Object obj = clazz.newInstance();
               System.out.println(obj);
           } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
               e.printStackTrace();
           }
       }
   }
   ```

类加载器的作用

1. **类的隔离**：
   - 不同的类加载器可以加载相同名称的类，但这些类在 JVM 中被视为不同的类。这对于实现类的隔离和多租户环境非常有用。

2. **动态加载**：
   - 类加载器可以按需加载类，支持动态加载和热部署。

3. **安全性**：
   - 通过双亲委派模型，确保核心类库的安全性，防止恶意代码覆盖标准库中的类。

总结：类加载器是 JVM 中一个非常重要的组件，负责类的加载、验证、准备、解析和初始化。双亲委派模型确保了类的唯一性和安全性。通过自定义类加载器，开发者可以实现特定的类加载逻辑，满足不同应用场景的需求。理解类加载器的工作原理和机制，对于开发高效、安全的 Java 应用程序至关重要。