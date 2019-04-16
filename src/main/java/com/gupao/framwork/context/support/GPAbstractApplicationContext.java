package com.gupao.framwork.context.support;

/**
 * IOC容器的顶层设计
 */
public abstract class GPAbstractApplicationContext {
//    受保护的，只提供给子类重写
    protected void refresh() throws Exception{};
}
