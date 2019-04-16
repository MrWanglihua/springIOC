package com.gupao.framwork.context;

/**
 * 通过解耦的方式获得IOC容器的顶层设计
 * 后面将通过一个监听器，去扫描所以的类，只要实现了此接口
 * 将自动调用setApplicationContext方法，从而将IOC容器注入到目标容器中
 */
public interface GPApplicationContextAware{

    void setApplicationContext(GPApplicationContext applicationContext);

}
