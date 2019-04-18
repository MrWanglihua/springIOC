package com.gupao.framwork.aop.intercept;

public interface GPMethodInterceptor {
    Object invoke(GPMethodInvocation invocation) throws Throwable;
}
