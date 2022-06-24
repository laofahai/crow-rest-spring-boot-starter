package org.teamswift.crow.rest.utils;

import org.springframework.context.ApplicationContext;

import java.util.Map;

public class CrowBeanUtils {

    public static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static <T> T getBean(Class<T> c){
        return applicationContext.getBean(c);
    }

    public static <T> Map<String, T> getBeans(Class<T> c){
        return applicationContext.getBeansOfType(c);
    }

}
