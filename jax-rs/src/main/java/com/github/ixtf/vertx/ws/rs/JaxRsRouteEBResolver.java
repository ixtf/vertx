package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.route.RouteEBRepresentation;
import com.github.ixtf.vertx.util.RepresentationResolver;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Slf4j
public abstract class JaxRsRouteEBResolver extends RepresentationResolver<RouteEBRepresentation> {

    @Override
    public Stream<? extends RouteEBRepresentation> resolve() {
        return classStream().flatMap(JaxRs::resourceStream).filter(JaxRs.resourceFilter())
                .collect(toSet()).parallelStream()
                .map(JaxRsResource::new)
                .flatMap(JaxRsResource::routes)
                .map(it -> JaxRsRouteEB.create(it, this::getProxy));
    }

    protected Object getProxy(Method method) {
        final Class<?> declaringClass = method.getDeclaringClass();
        return getProxy(declaringClass);
    }

    protected abstract Object getProxy(Class<?> clazz);

}
