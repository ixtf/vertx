package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.RouteEBRepresentation;
import com.github.ixtf.vertx.RouteRepresentation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
class JaxRsRouteEBRepresentation extends RouteEBRepresentation {

    protected JaxRsRouteEBRepresentation(RouteRepresentation routeRepresentation, Object proxy, Method method) {
        super(routeRepresentation, proxy, method);
    }

    static JaxRsRouteEBRepresentation create(JaxRsRouteRepresentation routeRepresentation, Function<Method, Object> proxyFun) {
        final Method method = routeRepresentation.getMethod();
        final Object proxy = proxyFun.apply(method);
        return new JaxRsRouteEBRepresentation(routeRepresentation, proxy, method);
    }

}