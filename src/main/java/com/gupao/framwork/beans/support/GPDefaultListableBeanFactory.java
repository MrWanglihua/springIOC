package com.gupao.framwork.beans.support;

import com.gupao.framwork.beans.config.GPBeanDefinition;
import com.gupao.framwork.context.support.GPAbstractApplicationContext;
import com.gupao.framwork.core.GPBeanFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GPDefaultListableBeanFactory extends GPAbstractApplicationContext {
    //存储注册信息的：BeanDefinition()
    /**
     * IOC容器
     */
    protected final Map<String, GPBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String,GPBeanDefinition>();

}
