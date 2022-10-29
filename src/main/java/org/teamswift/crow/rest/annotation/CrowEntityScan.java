package org.teamswift.crow.rest.annotation;

import org.springframework.context.annotation.Import;
import org.teamswift.crow.rest.configure.CrowEntityRegistrar;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(CrowEntityRegistrar.class)
public @interface CrowEntityScan {
    String[] basePackages() default {};
}
