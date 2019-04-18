package com.gupao.framwork.aop.aspect;

import java.lang.reflect.Method;

public class GPAbstractAspectAdvice implements GPAdvice{

    private Method aspectMethod;
    private Object aspectTarget;

    public GPAbstractAspectAdvice(Method aspectMethod, Object aspectTarget) {
        this.aspectMethod = aspectMethod;
        this.aspectTarget = aspectTarget;
    }

    public Object invokeAdviceMethod(GPJoinPoint joinPoint, Object returnValue, Throwable tx)throws Exception{
        Class<?>[] parameterTypes = this.aspectMethod.getParameterTypes();
        if(parameterTypes==null || "".equals(parameterTypes)){
            return this.aspectMethod.invoke(aspectTarget);
        }else {
            Object[] args = new Object[parameterTypes.length];
            for (int i = 0; i < args.length; i++) {
                if(parameterTypes[i] == GPJoinPoint.class){
                    args[i] = joinPoint;
                }else if(parameterTypes[i] == Throwable.class){
                    args[i] = tx;
                }else if(parameterTypes[i] == Object.class){
                    args[i] = returnValue;
                }
            }

            return this.aspectMethod.invoke(aspectTarget,args);
        }
    }

}
