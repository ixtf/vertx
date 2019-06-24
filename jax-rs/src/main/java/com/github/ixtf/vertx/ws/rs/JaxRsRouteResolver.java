package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.route.RouteRepresentation;
import com.github.ixtf.vertx.util.RepresentationResolver;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Path;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Slf4j
public abstract class JaxRsRouteResolver extends RepresentationResolver<RouteRepresentation> {

    @Override
    public Stream<? extends RouteRepresentation> resolve() {
        return classStream().flatMap(JaxRs::resourceStream)
                .filter(it -> it.getAnnotation(Path.class) != null)
                .collect(toSet()).parallelStream()
                .map(JaxRsResource::new)
                .flatMap(JaxRsResource::routes)
                .peek(it -> log.info("address=" + it.getAddress()));
    }

}
