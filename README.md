AOP
============

**基于 `Javassist` 和 `asm` 分别实现的一套AOP方案**，可分析和修改任意一条字节码指令.




目前实现的功能如下
==============

* 编译时打印出任意一个函数的调用位置，结果打印在 ./apm 目录下
* 开发者可以设置任意一个函数的运行时回调

使用方法
====

如下修改工程根目录下的 build.gradle 文件

```groovy
buildscript {

  repositories {
        google()
        jcenter()
        maven { url "https://dl.bintray.com/onepiece/maven" }
  }
    
  dependencies {
    classpath 'com.hhh.onepiece:plugin:1.0.0'
  }
}
```

在你的 app 工程目录下应用这个插件，`ASMPlugin` 是用 `asm` 实现的，`JavassistPlugin` 是用`Javassist` 实现的，方式不同，结果一样

```groovy
apply plugin: 'ASMPlugin'
//apply plugin: 'JavassistPlugin'
```

具体用法
====

```java
import android.util.Log;

import com.hhh.annotation.InvokerTrack;
import com.hhh.annotation.InvokerTrackMark;

@InvokerTrackMark
public class InvokerTrackUtils {

  @InvokerTrack(className = "android.view.LayoutInflater", methodName = "inflate")
  public static void track_LayoutInflater_inflate() {
    Log.d(Constants.TAG, "aop", new RuntimeException("track_LayoutInflater_inflate"));
  }

  @InvokerTrack(className = "java.lang.Thread", methodName = "sleep")
  public static void track_Thread_sleep() {
    Log.d(Constants.TAG, "aop", new RuntimeException("track_Thread_sleep"));
  }
  
  ...
}

```

Contributing
============

* 此库刚设计完，设计和功能都较为简单，但是已经可以分析和操作任意一条字节码，前景无限，只有想不到，没有做不到
* 欢迎提Issue与作者交流
* 欢迎提Pull request，帮助 fix bug，增加新的feature，让此插件变得更强大、更好用
