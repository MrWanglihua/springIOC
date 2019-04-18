package com.gupao.framwork.aop.aspect;

import java.lang.reflect.Method;

public class GPAfterReturningAdviceInterceptor extends GPAbstractAspectAdvice implements GPAdvice{
    public GPAfterReturningAdviceInterceptor(Method method, Object newInstance) {
        super(method,newInstance);
    }
}
