package com.gupao.framwork.aop.aspect;

import com.gupao.framwork.aop.intercept.GPMethodInterceptor;
import com.gupao.framwork.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPDoAroundAdviceInterceptor extends GPAbstractAspectAdvice implements GPAdvice, GPMethodInterceptor {
    private GPJoinPoint joinPoint;
    public GPDoAroundAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    public Object doAround(Method method, Object[] args, Object target) throws Throwable {
        return invokeAdviceMethod(this.joinPoint,null,null);
    }

    @Override
    public Object invoke(GPMethodInvocation invocation) throws Throwable {
        return doAround(invocation.getMethod(),invocation.getArguments(),invocation.getThis());
    }


}
