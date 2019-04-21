package com.gupao.framwork.aop;

import com.gupao.framwork.aop.support.GPAdvisedSupport;

public class GPCglibAopProxy implements GPAopProxy{
    public GPCglibAopProxy(GPAdvisedSupport config) {
        
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
