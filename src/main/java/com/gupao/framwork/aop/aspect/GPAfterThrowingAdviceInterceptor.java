package com.gupao.framwork.aop.aspect;

import com.gupao.framwork.aop.intercept.GPMethodInterceptor;
import com.gupao.framwork.aop.intercept.GPMethodInvocation;
import lombok.Data;

import java.lang.reflect.Method;
@Data
public class GPAfterThrowingAdviceInterceptor extends GPAbstractAspectAdvice implements GPAdvice, GPMethodInterceptor {

    private String throwingName;
    public GPAfterThrowingAdviceInterceptor(Method method, Object newInstance) {
        super(method,newInstance);
    }



    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        }catch (Throwable e){
            invokeAdviceMethod(mi,null,e.getCause());
            throw e;
        }
    }

    public void setThrowName(String throwName){
        this.throwingName = throwName;
    }
}
