package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.route.RouteRepresentation;
import io.vertx.core.http.HttpMethod;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.Optional;

import static com.github.ixtf.vertx.Jvertx.API;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
class JaxRsRoute extends RouteRepresentation {
    @Getter
    private final JaxRsResource jaxRsResource;
    @Getter
    private final String jaxRsPath;
    @Getter
    private final String[] jaxRsConsumes;
    @Getter
    private final String[] jaxRsProduces;

    private JaxRsRoute(Method method, HttpMethod httpMethod, String path, String[] consumes, String[] produces, String address, JaxRsResource jaxRsResource, String jaxRsPath, String[] jaxRsConsumes, String[] jaxRsProduces) {
        super(method, httpMethod, path, consumes, produces, address);
        this.jaxRsResource = jaxRsResource;
        this.jaxRsPath = jaxRsPath;
        this.jaxRsConsumes = jaxRsConsumes;
        this.jaxRsProduces = jaxRsProduces;
    }

    static JaxRsRoute create(JaxRsResource jaxRsResource, Method method, HttpMethod httpMethod) {
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
        return new JaxRsRoute(method, httpMethod, path, consumes, produces, address, jaxRsResource, jaxRsPath, jaxRsConsumes, jaxRsProduces);
    }

}