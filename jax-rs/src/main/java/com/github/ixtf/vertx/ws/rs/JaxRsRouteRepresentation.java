package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.RouteRepresentation;
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
class JaxRsRouteRepresentation extends RouteRepresentation {
    @Getter
    private final JaxRsResourceRepresentation resourceRepresentation;
    @Getter
    private final Method method;
    @Getter
    private final String jaxRsPath;
    @Getter
    private final String[] jaxRsConsumes;
    @Getter
    private final String[] jaxRsProduces;

    private JaxRsRouteRepresentation(HttpMethod httpMethod, String path, String[] consumes, String[] produces, String address, JaxRsResourceRepresentation resourceRepresentation, Method method, String jaxRsPath, String[] jaxRsConsumes, String[] jaxRsProduces) {
        super(httpMethod, path, consumes, produces, address);
        this.resourceRepresentation = resourceRepresentation;
        this.method = method;
        this.jaxRsPath = jaxRsPath;
        this.jaxRsConsumes = jaxRsConsumes;
        this.jaxRsProduces = jaxRsProduces;
    }

    static JaxRsRouteRepresentation create(JaxRsResourceRepresentation resourceRepresentation, Method method, HttpMethod httpMethod) {
        final String[] jaxRsConsumes = JaxRs.getConsumes(method);
        final String[] consumes = Optional.ofNullable(jaxRsConsumes)
                .filter(ArrayUtils::isNotEmpty)
                .orElseGet(resourceRepresentation::getJaxRsConsumes);

        final String[] jaxRsProduces = JaxRs.getProduces(method);
        final String[] produces = Optional.ofNullable(jaxRsProduces)
                .filter(ArrayUtils::isNotEmpty)
                .orElseGet(resourceRepresentation::getJaxRsProduces);

        final String jaxRsPath = JaxRs.getPath(method);
        final String path = JaxRs.vertxPath(resourceRepresentation.getJaxRsPath(), jaxRsPath);

        final String address = API + ":" + httpMethod.name() + ":" + path;
        return new JaxRsRouteRepresentation(httpMethod, path, consumes, produces, address, resourceRepresentation, method, jaxRsPath, jaxRsConsumes, jaxRsProduces);
    }

}