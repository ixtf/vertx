package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.util.RepresentationResolver;
import com.github.ixtf.vertx.route.RouteRepresentation;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Slf4j
public abstract class JaxRsRouteResolver extends RepresentationResolver<RouteRepresentation> {

    @Override
    public Stream<? extends RouteRepresentation> resolve() {
        return classes().flatMap(JaxRs::resourceStream).filter(JaxRs.resourceFilter())
                .collect(toSet()).parallelStream()
                .map(JaxRsResource::new)
                .flatMap(JaxRsResource::routes)
                .peek(it -> log.info("api=" + it.getPath() + "\t address=" + it.getAddress()));
    }

}