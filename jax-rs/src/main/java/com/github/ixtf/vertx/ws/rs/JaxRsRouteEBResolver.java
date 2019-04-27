package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.RepresentationResolver;
import com.github.ixtf.vertx.RouteEBRepresentation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Slf4j
public abstract class JaxRsRouteEBResolver extends RepresentationResolver<RouteEBRepresentation> {

    @Override
    protected Stream<? extends RouteEBRepresentation> resolve() {
        return classes().filter(JaxRs.resourceFilter())
                .collect(toSet()).parallelStream()
                .map(JaxRsResourceRepresentation::new)
                .flatMap(JaxRsResourceRepresentation::routes)
                .map(it -> JaxRsRouteEBRepresentation.create(it, this::getProxy));
    }

    protected Object getProxy(Method method) {
        final Class<?> declaringClass = method.getDeclaringClass();
        return getProxy(declaringClass);
    }

    protected abstract Object getProxy(Class<?> clazz);
}
