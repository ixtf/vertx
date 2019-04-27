package com.github.ixtf.vertx.jax_rs.api;

import com.github.ixtf.vertx.api.AbstractResourceRepresentation;
import com.github.ixtf.vertx.api.RouteRepresentation;
import com.github.ixtf.vertx.spi.ResourceProvider;
import io.vertx.core.http.HttpMethod;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.apache.commons.lang3.reflect.MethodUtils.getMethodsListWithAnnotation;

/**
 * todo 支持subResource
 *
 * @author jzb 2019-02-16
 */
@Slf4j
public class ResourceRepresentationJaxRs extends AbstractResourceRepresentation {
    @Getter
    private final String jaxRsPath;
    @Getter
    private final String[] jaxRsConsumes;
    @Getter
    private final String[] jaxRsProduces;
    private final RouteRepresentation[] routes;

    public ResourceRepresentationJaxRs(ResourceProvider resourceProvider, Class<?> resourceClass) {
        super(resourceProvider, resourceClass);
        jaxRsPath = JaxRs.getPath(resourceClass);
        jaxRsConsumes = JaxRs.getConsumes(resourceClass);
        jaxRsProduces = JaxRs.getProduces(resourceClass);
        routes = initRoutes(resourceClass);
    }

    private RouteRepresentation[] initRoutes(Class resourceClazz) {
        Stream<RouteRepresentation> result = Stream.empty();
        Stream<RouteRepresentation> routeStream = getMethodsListWithAnnotation(resourceClazz, GET.class).stream()
                .map(it -> new RouteRepresentationJaxRs(this, it, HttpMethod.GET));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClazz, PUT.class).stream()
                .map(it -> new RouteRepresentationJaxRs(this, it, HttpMethod.PUT));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClazz, POST.class).stream()
                .map(it -> new RouteRepresentationJaxRs(this, it, HttpMethod.POST));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClazz, DELETE.class).stream()
                .map(it -> new RouteRepresentationJaxRs(this, it, HttpMethod.DELETE));
        result = Stream.concat(result, routeStream);

        return result.toArray(RouteRepresentation[]::new);
    }

    @Override
    public Stream<RouteRepresentation> routes() {
        return Arrays.stream(routes);
    }
}
