package org.teamswift.crow.rest.configure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.teamswift.crow.rest.utils.CrowBeanUtils;

@Configuration
public class CrowLocaleBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof MessageSource) {
            ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();

            messageSource.setBasenames(
                    "i18n/messages",
                    "i18n/crow-rest"
            );
            messageSource.setDefaultEncoding("UTF-8");

            return messageSource;
        } else {
            return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
        }
    }
}
