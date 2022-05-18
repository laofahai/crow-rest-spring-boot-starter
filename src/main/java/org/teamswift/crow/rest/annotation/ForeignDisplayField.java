package org.teamswift.crow.rest.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ForeignDisplayField {

    String value();

}
