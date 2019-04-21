package com.gupao.framwork.aop;

import com.gupao.framwork.aop.intercept.GPMethodInvocation;
import com.gupao.framwork.aop.support.GPAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class GPJdkDynamicAopProxy implements GPAopProxy, InvocationHandler {

    private GPAdvisedSupport advise;

    public GPJdkDynamicAopProxy(GPAdvisedSupport advise) {
        this.advise = advise;
    }

    @Override
    public Object getProxy() {
        return this.getProxy(advise.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        //        this.advised.getTargetClass().getInterfaces()：获得切面类的初始化
        return Proxy.newProxyInstance(classLoader,this.advise.getTargetClass().getInterfaces(),this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        List<Object> interceptorsAndDynamicInterceptionAdvice = this.advise.getInterceptorsAndDynamicInterceptionAdvice(method, advise.getTargetClass());
        GPMethodInvocation invocation =new GPMethodInvocation(proxy,this.advise.getTarget()
                ,method,args,this.advise.getTargetClass(),interceptorsAndDynamicInterceptionAdvice);
        return invocation.proceed();
    }
}
