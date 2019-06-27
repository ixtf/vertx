package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.route.RouteRepresentation;
import io.vertx.core.http.HttpMethod;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.Optional;

import static com.github.ixtf.vertx.Jvertx.API;

/**
 * @author jzb 2019-02-14
 */
class JaxRsRoute extends RouteRepresentation {
    @Getter
    private final JaxRsResource jaxRsResource;
    @Getter
    private final String jaxRsPath;
    @Getter
    private final String[] jaxRsConsumes;
    @Getter
    private final String[] jaxRsProduces;

    private JaxRsRoute(HttpMethod httpMethod, String path, String address, String[] consumes, String[] produces, JaxRsResource jaxRsResource, Method method, String jaxRsPath, String[] jaxRsConsumes, String[] jaxRsProduces) {
        super(httpMethod, path, address, consumes, produces, method);
        this.jaxRsResource = jaxRsResource;
        this.jaxRsPath = jaxRsPath;
        this.jaxRsConsumes = jaxRsConsumes;
        this.jaxRsProduces = jaxRsProduces;
    }

    static JaxRsRoute create(HttpMethod httpMethod, JaxRsResource jaxRsResource, Method method) {
        final String[] jaxRsConsumes = JaxRs.getConsumes(method);
        final String[] consumes = Optional.ofNullable(jaxRsConsumes)
                .filter(ArrayUtils::isNotEmpty)
                .orElseGet(jaxRsResource::getJaxRsConsumes);

        final String[] jaxRsProduces = JaxRs.getProduces(method);
        final String[] produces = Optional.ofNullable(jaxRsProduces)
                .filter(ArrayUtils::isNotEmpty)
                .orElseGet(jaxRsResource::getJaxRsProduces);

        final String jaxRsPath = JaxRs.getPath(method);
        final String path = JaxRs.vertxPath(jaxRsResource.getJaxRsPath(), jaxRsPath);

        final String address = API + ":" + httpMethod.name() + ":" + path;
        return new JaxRsRoute(httpMethod, path, address, consumes, produces, jaxRsResource, method, jaxRsPath, jaxRsConsumes, jaxRsProduces);
    }

}