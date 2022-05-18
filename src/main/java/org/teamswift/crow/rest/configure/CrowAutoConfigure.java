package org.teamswift.crow.rest.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dozer.DozerBeanMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.teamswift.crow.rest.utils.CrowBeanUtils;

@Configuration
@EnableConfigurationProperties(CrowServiceProperties.class)
public class CrowAutoConfigure {

    private final CrowServiceProperties properties;

    public CrowAutoConfigure(CrowServiceProperties properties, ApplicationContext context) {
        this.properties = properties;
        CrowBeanUtils.setApplicationContext(context);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "crow.starter", value = "enabled", havingValue = "true")
    public DozerBeanMapper dozerBeanMapper() {
        return new DozerBeanMapper();
    }


}
