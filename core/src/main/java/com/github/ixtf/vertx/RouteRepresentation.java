package com.github.ixtf.vertx;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.ext.web.Route;
import io.vertx.reactivex.ext.web.Router;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * @author jzb 2019-02-14
 */
public abstract class RouteRepresentation {
    @Getter
    protected final HttpMethod httpMethod;
    @Getter
    protected final String path;
    @Getter
    protected final String[] consumes;
    @Getter
    protected final String[] produces;
    @Getter
    protected final String address;

    protected RouteRepresentation(HttpMethod httpMethod, String path, String[] consumes, String[] produces, String address) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.consumes = consumes;
        this.produces = produces;
        this.address = address;
    }

    protected DeliveryOptions getDeliveryOptions() {
        return new DeliveryOptions();
    }

    public void router(Router router) {
        final Route route = router.route(httpMethod, path);
        if (ArrayUtils.isNotEmpty(consumes)) {
            Arrays.stream(consumes).forEach(route::consumes);
        }
        if (ArrayUtils.isNotEmpty(produces)) {
            Arrays.stream(produces).forEach(route::produces);
        }
        route.handler(rc -> RCEnvelope.send(rc, address, getDeliveryOptions()));
    }

}