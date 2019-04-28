package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.route.RouteEBRepresentation;
import com.github.ixtf.vertx.route.RouteRepresentation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
class JaxRsRouteEB extends RouteEBRepresentation {

    protected JaxRsRouteEB(RouteRepresentation routeRepresentation, Object proxy) {
        super(routeRepresentation, proxy);
    }

    static JaxRsRouteEB create(JaxRsRoute jaxRsRoute, Function<Method, Object> proxyFun) {
        final Method method = jaxRsRoute.getMethod();
        final Object proxy = proxyFun.apply(method);
        return new JaxRsRouteEB(jaxRsRoute, proxy);
    }

}