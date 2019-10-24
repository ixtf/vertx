package com.github.ixtf.vertx;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.DeliveryOptions;

import java.lang.annotation.*;

/**
 * @author jzb 2019-02-28
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JvertxOptions {
    /**
     * @return 超时，毫秒
     */
    long timeout() default DeliveryOptions.DEFAULT_TIMEOUT;

    /**
     * @return 超时，毫秒
     */
    int instances() default DeploymentOptions.DEFAULT_INSTANCES;

    String apmService() default "";
}
