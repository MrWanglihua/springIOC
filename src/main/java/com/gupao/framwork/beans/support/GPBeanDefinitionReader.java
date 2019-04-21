package com.gupao.framwork.beans.support;

import com.gupao.framwork.beans.config.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 读取配置文件，将其存入IOC容器中
 */
public class GPBeanDefinitionReader {

    private List<String> registyBeanClass = new ArrayList<String>();

    private Properties config = new Properties();

    //固定配置文件中的 key，相对于 xml 的规范
    private final String SCAN_PACKAGE = "scanPackage";

    /**
     * 构造方法，将配置文件路径转换成property实体，将其解析
     *
     * @param locations
     */
    public GPBeanDefinitionReader(String... locations) {

        for(String   s:locations){
            System.out.println("====》"+s);
        }

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replaceAll("classpath:", ""));

        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(this.SCAN_PACKAGE));

    }

    /**
     * 扫描配置文件中的信息，将他们注入到IOC容器中
     * @return
     */
    public List<GPBeanDefinition> loadBeanDefinitions() {

        List<GPBeanDefinition> result = new ArrayList<GPBeanDefinition>();
        try {
            for (String className : registyBeanClass) {
                Class<?> beanClass = Class.forName(className);
                //如果是一个接口，是不能实例化的
                //用它实现类来实例化
                if (beanClass.isInterface()) {
                    continue;
                }

                //beanName有三种情况:
                //1、默认是类名首字母小写
                //2、自定义名字
                //3、接口注入
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));


                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> i:interfaces) {
                    //如果是多个实现类，只能覆盖
                    //为什么？因为Spring没那么智能，就是这么傻
                    //这个时候，可以自定义名字
                        result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }


            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 将每一个配置信息，解析成BeanDefinition
     *
     * @param factoryBeanName
     * @param beanClassName
     * @return
     */
    private GPBeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        GPBeanDefinition beanDefinition = new GPBeanDefinition();
        beanDefinition.setFactoryBeanName(factoryBeanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }


    /**
     * 解析配置文件，将对应配置转
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {

        //scanPackage = com.gupaoedu.demo ，存储的是包路径
        //转换为文件路径，实际上就是把.替换为/就OK了
        //classpath
        URL url = this.getClass().getResource("/" + scanPackage.replaceAll("\\.", "/"));

        File classPath = new File(url.getFile());


        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                this.doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) continue;
                String name = scanPackage + "." + file.getName().replaceAll(".class", "");
                registyBeanClass.add(name);
            }
        }


    }

    public Properties getConfig() {
        return this.config;
    }


    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        //之所以加，是因为大小写字母的ASCII码相差32，
        // 而且大写字母的ASCII码要小于小写字母的ASCII码
        //在Java中，对char做算学运算，实际上就是对ASCII码做算学运算
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
