package com.gupao.framwork.aop.aspect;

import com.gupao.framwork.aop.intercept.GPMethodInterceptor;
import com.gupao.framwork.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPAfterReturningAdviceInterceptor extends GPAbstractAspectAdvice implements GPAdvice, GPMethodInterceptor {
    private GPJoinPoint joinPoint;
    public GPAfterReturningAdviceInterceptor(Method method, Object newInstance) {
        super(method,newInstance);
    }

    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal,mi.getMethod(),mi.getArguments(),mi.getThis());
        return retVal;
    }

    private void afterReturning(Object retVal, Method method, Object[] arguments, Object aThis) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint,retVal,null);
    }
}
