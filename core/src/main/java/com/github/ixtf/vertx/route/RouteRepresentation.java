package com.github.ixtf.vertx.route;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.vertx.core.http.HttpMethod.*;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RouteRepresentation {
    @EqualsAndHashCode.Include
    @Getter
    private final HttpMethod httpMethod;
    @EqualsAndHashCode.Include
    @Getter
    private final String path;
    @Getter
    private final String address;
    private final String[] consumes;
    private final String[] produces;
    @Getter
    private final Method method;

    protected RouteRepresentation(HttpMethod httpMethod, String path, String address, String[] consumes, String[] produces, Method method) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.address = address;
        this.consumes = consumes;
        this.produces = produces;
        this.method = method;
    }

    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        return Optional.ofNullable(method.getAnnotation(clazz)).orElseGet(() -> {
            final Class<?> declaringClass = method.getDeclaringClass();
            return declaringClass.getAnnotation(clazz);
        });
    }

    public Route router(Router router, Function<Class, Object> proxyFun) {
        final Route route = router.route(httpMethod, path);
        if (ArrayUtils.isNotEmpty(consumes)) {
            if (Set.of(POST, PUT, PATCH).contains(httpMethod)) {
                Arrays.stream(consumes).forEach(route::consumes);
            }
        }
        if (ArrayUtils.isNotEmpty(produces)) {
            Arrays.stream(produces).forEach(route::produces);
        }
        return route.handler(new RouteRepresentationHandler(this, proxyFun));
    }

    public Future<String> consumer(Vertx vertx, Function<Class, Object> proxyFun, Function<JsonObject, Principal> principalFun) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
        final Supplier<Verticle> verticleSupplier = () -> new RouteRepresentationConsumer(this, proxyFun, principalFun);
        return Future.future(p -> vertx.deployVerticle(verticleSupplier, deploymentOptions, p));
    }

    public Future<String> consumer(Vertx vertx, Function<Class, Object> proxyFun) {
        return consumer(vertx, proxyFun, RoutingContextEnvelope::defaultPrincipalFun);
    }

}