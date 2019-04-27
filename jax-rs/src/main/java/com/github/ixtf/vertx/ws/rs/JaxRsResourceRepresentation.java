package com.github.ixtf.vertx.ws.rs;

import io.vertx.core.http.HttpMethod;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.util.stream.Stream;

import static org.apache.commons.lang3.reflect.MethodUtils.getMethodsListWithAnnotation;

/**
 * todo 支持subResource
 *
 * @author jzb 2019-02-16
 */
@Slf4j
class JaxRsResourceRepresentation {
    @Getter
    private final Class<?> resourceClass;
    @Getter
    private final String jaxRsPath;
    @Getter
    private final String[] jaxRsConsumes;
    @Getter
    private final String[] jaxRsProduces;

    public JaxRsResourceRepresentation(Class<?> resourceClass) {
        this.resourceClass = resourceClass;
        jaxRsPath = JaxRs.getPath(this.resourceClass);
        jaxRsConsumes = JaxRs.getConsumes(this.resourceClass);
        jaxRsProduces = JaxRs.getProduces(this.resourceClass);
    }

    public Stream<JaxRsRouteRepresentation> routes() {
        Stream<JaxRsRouteRepresentation> result = Stream.empty();
        Stream<JaxRsRouteRepresentation> routeStream = getMethodsListWithAnnotation(resourceClass, GET.class).stream()
                .map(it -> JaxRsRouteRepresentation.create(this, it, HttpMethod.GET));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClass, PUT.class).stream()
                .map(it -> JaxRsRouteRepresentation.create(this, it, HttpMethod.PUT));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClass, POST.class).stream()
                .map(it -> JaxRsRouteRepresentation.create(this, it, HttpMethod.POST));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClass, DELETE.class).stream()
                .map(it -> JaxRsRouteRepresentation.create(this, it, HttpMethod.DELETE));
        return Stream.concat(result, routeStream);
    }

}
