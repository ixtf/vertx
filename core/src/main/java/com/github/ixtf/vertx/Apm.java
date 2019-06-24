package com.github.ixtf.vertx;

import java.lang.annotation.*;

/**
 * @author jzb 2019-02-28
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Apm {
}
