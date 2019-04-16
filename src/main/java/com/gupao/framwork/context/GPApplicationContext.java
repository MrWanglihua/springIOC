package com.gupao.framwork.context;

import com.gupao.framwork.beans.config.GPBeanDefinition;
import com.gupao.framwork.beans.support.GPBeanDefinitionReader;
import com.gupao.framwork.beans.support.GPDefaultListableBeanFactory;
import com.gupao.framwork.core.GPBeanFactory;

import java.util.List;
import java.util.Map;

/**
 * GPApplicationContext：最底层的容器的实现
 * GPDefaultListableBeanFactory：默认的实现
 * GPBeanFactory   ：定义了一个规范
 *
 * 逻辑顺序：IOC，DI,MVC,AOP
 *
 */
public class GPApplicationContext extends GPDefaultListableBeanFactory implements  GPBeanFactory{


    private String[] configLoactions;
    private GPBeanDefinitionReader reader;

    public GPApplicationContext(String... configLoactions) {
        this.configLoactions = configLoactions;
        try {
            this.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void refresh() throws Exception {

//        1、定位配置信息
         reader = new GPBeanDefinitionReader(this.configLoactions);
//        2、加载配置文件，扫描相关的类，把他们封装成BeanDefinition
        List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
//        3、注册，把配置信息放到IOC容器中（伪IOC容器）
        doRegisterBeanDefinition(beanDefinitions);
//        4、把不是延迟加载的类，提前初始化

        doAotowrited();
    }

    /**
     * 只处理非延迟加载的情况
     */
    private void doAotowrited() {

        for (Map.Entry<String,GPBeanDefinition> entry:beanDefinitionMap.entrySet()) {

            String beanName = entry.getKey();

            if(!entry.getValue().isLazyInit()){
                this.getBean(beanName);
            }

        }

    }

    private void doRegisterBeanDefinition(List<GPBeanDefinition> beanDefinitions) throws Exception {
        for (GPBeanDefinition definition:beanDefinitions) {
            if(super.beanDefinitionMap.containsKey(definition.getFactoryBeanName())){
                throw new Exception("The “" + definition.getFactoryBeanName() + "” is exists!!");
            }
            super.beanDefinitionMap.put(definition.getFactoryBeanName(),definition);

            //到这里为止，容器初始化完毕
        }

    }

    @Override
    public Object getBean(String beanName) {
        return null;
    }
}
