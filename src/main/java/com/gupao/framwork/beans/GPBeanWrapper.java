package com.gupao.framwork.beans;

import lombok.Getter;

public class GPBeanWrapper {
    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public GPBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    // 返回代理以后的 Class
    // 可能会是这个 $Proxy0
    public Class<?> getWrappedClass() {
        return wrappedClass;
    }
}
