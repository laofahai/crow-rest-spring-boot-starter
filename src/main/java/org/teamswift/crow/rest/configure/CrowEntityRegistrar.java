package org.teamswift.crow.rest.configure;

import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.teamswift.crow.rest.annotation.CrowEntity;
import org.teamswift.crow.rest.annotation.CrowEntityScan;
import org.teamswift.crow.rest.service.CrowDataStructureService;

import javax.persistence.Entity;
import java.io.IOException;
import java.util.*;

@Configuration
public class CrowEntityRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Autowired private CrowDataStructureService crowDataStructureService;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ImportBeanDefinitionRegistrar.super.registerBeanDefinitions(importingClassMetadata, registry);

        AnnotationAttributes annotationAttributes =
                AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(CrowEntityScan.class.getName()));

        assert annotationAttributes != null;
        String[] basePackages = annotationAttributes.getStringArray("basePackages");
        List<String> packages = new ArrayList<>(Arrays.asList(basePackages));
        initDataStructure(packages);
    }

    private void initDataStructure(List<String> basePackages) {
        ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        MetadataReaderFactory metaReader = new CachingMetadataReaderFactory(resourceLoader);

        basePackages.add("org.teamswift.crow");

        Set<Class<?>> classes = new HashSet<>();
        for(String basePackage: basePackages) {
            Resource[] resources;

            try {
                String resourcesLocation = String.format("classpath*:%s/**/*.class", basePackage.replaceAll("\\.", "/"));
                resources = resolver.getResources(resourcesLocation);

                for (Resource r : resources) {
                    MetadataReader reader = metaReader.getMetadataReader(r);
                    String clsName = reader.getClassMetadata().getClassName();
                    Class<?> cls = Class.forName(clsName);
                    if(cls.isAnnotationPresent(Entity.class)
                            || cls.isAnnotationPresent(CrowEntity.class)
                            || cls.isAnnotationPresent(TableName.class)) {
                        classes.add(cls);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CrowDataStructureService.entityClasses = classes;
    }
}