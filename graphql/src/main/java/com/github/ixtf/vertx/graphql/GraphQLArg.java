package com.github.ixtf.vertx.graphql;

import java.lang.annotation.*;

/**
 * @author jzb 2019-10-05
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphQLArg {
    String value();
}
