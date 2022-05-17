package org.teamswift.crow.rest.utils;

import org.springframework.context.ApplicationContext;

public class CrowBeanUtils {

    //将管理上下文的applicationContext设置成静态变量，供全局调用
    public static ApplicationContext applicationContext;
    //定义一个获取已经实例化bean的方法
    public static <T> T getBean(Class<T> c){
        return applicationContext.getBean(c);
    }

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }
}
