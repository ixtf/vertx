package com.github.ixtf.vertx.route;

import com.github.ixtf.vertx.VertxDelivery;
import com.google.common.collect.ImmutableSet;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.ext.web.Route;
import io.vertx.reactivex.ext.web.Router;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

import static io.vertx.core.http.HttpMethod.*;

/**
 * @author jzb 2019-02-14
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class RouteRepresentation {
    @Getter
    protected final Method method;
    @EqualsAndHashCode.Include
    @Getter
    protected final HttpMethod httpMethod;
    @EqualsAndHashCode.Include
    @Getter
    protected final String path;
    @Getter
    protected final String[] consumes;
    @Getter
    protected final String[] produces;
    @Getter
    protected final String address;
    @Getter
    protected final Supplier<DeliveryOptions> deliveryOptionsSupplier;

    protected RouteRepresentation(Method method, HttpMethod httpMethod, String path, String[] consumes, String[] produces, String address) {
        this.method = method;
        this.httpMethod = httpMethod;
        this.path = path;
        this.consumes = consumes;
        this.produces = produces;
        this.address = address;

        final VertxDelivery vertxDelivery = method.getAnnotation(VertxDelivery.class);
        if (vertxDelivery == null) {
            deliveryOptionsSupplier = () -> new DeliveryOptions();
        } else {
            final long timeout = vertxDelivery.timeout();
            deliveryOptionsSupplier = () -> {
                final DeliveryOptions deliveryOptions = new DeliveryOptions();
                return deliveryOptions.setSendTimeout(timeout);
            };
        }
    }

    public void router(Router router) {
        final Route route = router.route(httpMethod, path);
        if (ArrayUtils.isNotEmpty(consumes)) {
            if (ImmutableSet.of(POST, PUT, PATCH).contains(httpMethod)) {
                Arrays.stream(consumes).forEach(route::consumes);
            }
        }
        if (ArrayUtils.isNotEmpty(produces)) {
            Arrays.stream(produces).forEach(route::produces);
        }
        route.handler(rc -> RCEnvelope.send(rc, address, deliveryOptionsSupplier.get()));
    }

}