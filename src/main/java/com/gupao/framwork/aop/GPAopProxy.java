package com.gupao.framwork.aop;

public interface GPAopProxy {
    Object getProxy();
    Object getProxy(ClassLoader classLoader);
}
