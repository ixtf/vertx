package com.github.ixtf.vertx.apm;

import java.lang.annotation.*;

/**
 * @author jzb 2019-02-28
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Apm {
    String service() default "";
}
