package com.gupao.framwork.beans.config;

import lombok.Data;

/**
 * 用来存储配置文件中的信息
 * 相当于保存在内存中的配置
 */
@Data
public class GPBeanDefinition {
//    对应的bean对应的类名
    private String beanClassName;
//    配置加载方式：是否需要延迟加载
    private boolean lazyInit = false;
//    对应的类加载的工厂名称
    private String factoryBeanName;

}
