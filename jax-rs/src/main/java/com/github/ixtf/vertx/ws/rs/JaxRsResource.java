package com.github.ixtf.vertx.ws.rs;

import io.vertx.core.http.HttpMethod;
import lombok.Getter;

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
class JaxRsResource {
    @Getter
    private final Class<?> resourceClass;
    @Getter
    private final String jaxRsPath;
    @Getter
    private final String[] jaxRsConsumes;
    @Getter
    private final String[] jaxRsProduces;

    public JaxRsResource(Class<?> resourceClass) {
        this.resourceClass = resourceClass;
        jaxRsPath = JaxRs.getPath(this.resourceClass);
        jaxRsConsumes = JaxRs.getConsumes(this.resourceClass);
        jaxRsProduces = JaxRs.getProduces(this.resourceClass);
    }

    public Stream<JaxRsRoute> routes() {
        Stream<JaxRsRoute> result = Stream.empty();
        Stream<JaxRsRoute> routeStream = getMethodsListWithAnnotation(resourceClass, GET.class).parallelStream()
                .map(it -> JaxRsRoute.create(HttpMethod.GET, this, it));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClass, PUT.class).parallelStream()
                .map(it -> JaxRsRoute.create(HttpMethod.PUT, this, it));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClass, POST.class).parallelStream()
                .map(it -> JaxRsRoute.create(HttpMethod.POST, this, it));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClass, DELETE.class).parallelStream()
                .map(it -> JaxRsRoute.create(HttpMethod.DELETE, this, it));
        return Stream.concat(result, routeStream).parallel();
    }

}
