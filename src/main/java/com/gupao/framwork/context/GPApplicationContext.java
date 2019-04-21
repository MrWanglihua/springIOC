package com.gupao.framwork.context;

import com.gupao.framwork.annotation.GPAutowired;
import com.gupao.framwork.annotation.GPController;
import com.gupao.framwork.annotation.GPService;
import com.gupao.framwork.aop.GPAopProxy;
import com.gupao.framwork.aop.GPCglibAopProxy;
import com.gupao.framwork.aop.GPJdkDynamicAopProxy;
import com.gupao.framwork.aop.config.GPAopConfig;
import com.gupao.framwork.aop.support.GPAdvisedSupport;
import com.gupao.framwork.beans.GPBeanWrapper;
import com.gupao.framwork.beans.config.GPBeanDefinition;
import com.gupao.framwork.beans.config.GPBeanPostProcessor;
import com.gupao.framwork.beans.support.GPBeanDefinitionReader;
import com.gupao.framwork.beans.support.GPDefaultListableBeanFactory;
import com.gupao.framwork.core.GPBeanFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GPApplicationContext：最底层的容器的实现
 * GPDefaultListableBeanFactory：默认的实现
 * GPBeanFactory   ：定义了一个规范
 * <p>
 * 逻辑顺序：IOC，DI,MVC,AOP
 */
public class GPApplicationContext extends GPDefaultListableBeanFactory implements GPBeanFactory {
    /**
     * 主要有两个方法
     * 1、getBean()--（回调BeanFactory中的方法,采用依赖注入的方式，给属性赋值)：返回对应的bean实例
     * 2、Refresh()--读取配置文件，填充IOC容器，并加载组件
     */

    private String[] configLoactions;
    private GPBeanDefinitionReader reader;


    //用来保存注册式单例的容器
    private Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();
    //用来存储所有被代理过的对象
    private Map<String, GPBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, GPBeanWrapper>();


    /**
     * 通过构造方法传入一个配置文件路径
     *
     * @param configLoactions refresh()：加载组件信息
     */
    public GPApplicationContext(String... configLoactions) {
        this.configLoactions = configLoactions;
        try {
            this.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分步加载配置文件
     *
     * @throws Exception
     */
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

        for (Map.Entry<String, GPBeanDefinition> entry : super.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();

            if (!entry.getValue().isLazyInit()) {
                try {
                    this.getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private void doRegisterBeanDefinition(List<GPBeanDefinition> beanDefinitions) throws Exception {
        for (GPBeanDefinition definition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(definition.getFactoryBeanName())) {
                throw new Exception("The “" + definition.getFactoryBeanName() + "” is exists!!");
            }
            super.beanDefinitionMap.put(definition.getFactoryBeanName(), definition);

            //到这里为止，容器初始化完毕
        }

    }




    /**
     * 依赖注入，从这里开始，通过读取 BeanDefinition 中的信息
     * 然后，通过反射机制创建一个实例并返回
     * Spring 做法是，不会把最原始的对象放出去，会用一个 BeanWrapper 来进行一次包装
     * 装饰器模式：
     * 1、保留原来的 OOP 关系
     * 2、我需要对它进行扩展，增强（为了以后 AOP 打基础）
     *
     * @param beanName
     * @return
     */
    @Override
    public Object getBean(String beanName) throws Exception {

        GPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

//            初始化一个通知
            GPBeanPostProcessor beanPostProcessor = new GPBeanPostProcessor();
//            通过beanDefinition创建bean实例
            Object instance = instantiateBean(beanDefinition);

            if (null == instance) {
                return null;
            }
//          在实例初始化之前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

            GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);
            this.factoryBeanInstanceCache.put(beanName, beanWrapper);
//          在实例初始化之后调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
//          将扫描到的类注入到初始化属性中
            populateBean(beanName,new GPBeanDefinition(),beanWrapper);


            return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();

    }

    /**
     * 将对象注入到属性中
     * @param beanName
     * @param gpBeanDefinition
     * @param gpBeanWrapper
     * 问题：传入参数是少了一个gpBeanWrapper,所以注入的时候为空
     */
    private void populateBean(String beanName, GPBeanDefinition gpBeanDefinition, GPBeanWrapper gpBeanWrapper) {
        Object instance = gpBeanWrapper.getWrappedInstance();

        Class<?> clazz = gpBeanWrapper.getWrappedClass();
        //不是所有牛奶都叫特仑苏
        if (!(clazz.isAnnotationPresent(GPController.class) || clazz.isAnnotationPresent(GPService.class))) {
            return;
        }

        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(GPAutowired.class)) {
                continue;
            }

            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);

            try {
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


        }


    }

    /**
     * 传入一个beanDefinition 返回一个bean实例,初始化bean
     *
     * @param beanDefinition
     */
    private Object instantiateBean(GPBeanDefinition beanDefinition) {

        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try {
            if (this.factoryBeanObjectCache.containsKey(className)) {

                instance = Class.forName(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();

                GPAdvisedSupport config = instantionAopConfig();
                config.setTargetClass(clazz);
                config.setTarget(instance);

                //符合PointCut的规则的话，创建代理对象
                if(config.pointCutMatch()) {
                    instance = createProxy(config).getProxy();
                }

//              错误：this.singletonBeanCacheMap.put(className,instance);没有，导致instance初始化为null
                this.factoryBeanObjectCache.put(className,instance);
                this.factoryBeanObjectCache.put(beanDefinition.getFactoryBeanName(), instance);

            }
            return instance;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }


        return null;
    }

    /**
     * 通过策略方法：创建代理对象
     * @param config
     * @return
     */
    private GPAopProxy createProxy(GPAdvisedSupport config) {

        Class<?> targetClass = config.getTargetClass();
        if(targetClass.getInterfaces().length>0){
            return new GPJdkDynamicAopProxy(config);
        }

        return new GPCglibAopProxy(config);
    }

    private GPAdvisedSupport instantionAopConfig() {
        GPAopConfig config = new GPAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setDoAround(this.reader.getConfig().getProperty("doAround"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new GPAdvisedSupport(config);
    }

    public String[] getBeanDefinitionNames() {
//        出错，原内容为return this.beanDefinitionMap.entrySet().toArray(new String[beanDefinitionMap.size()]);
        return this.beanDefinitionMap.keySet().toArray(new String[beanDefinitionMap.size()]);
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }

}
