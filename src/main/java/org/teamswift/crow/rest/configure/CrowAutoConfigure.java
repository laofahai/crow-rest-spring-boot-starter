package org.teamswift.crow.rest.configure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dozer.DozerBeanMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.teamswift.crow.rest.utils.CrowBeanUtils;

@Configuration
@EnableConfigurationProperties(CrowServiceProperties.class)
public class CrowAutoConfigure implements WebMvcConfigurer {

    private final CrowServiceProperties properties;

    public CrowAutoConfigure(CrowServiceProperties properties, ApplicationContext context) {
        this.properties = properties;
        CrowBeanUtils.setApplicationContext(context);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //添加映射路径
        registry.addMapping("/**")
                .allowCredentials(false)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "crow.starter", value = "enabled", havingValue = "true")
    public DozerBeanMapper dozerBeanMapper() {
        return new DozerBeanMapper();
    }


}
