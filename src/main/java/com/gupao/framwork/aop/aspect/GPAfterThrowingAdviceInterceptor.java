package com.gupao.framwork.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;
@Data
public class GPAfterThrowingAdviceInterceptor extends GPAbstractAspectAdvice implements GPAdvice{

    private String throwingName;
    public GPAfterThrowingAdviceInterceptor(Method method, Object newInstance) {
        super(method,newInstance);
    }
}
