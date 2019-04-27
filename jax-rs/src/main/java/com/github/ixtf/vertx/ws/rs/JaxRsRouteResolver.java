package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.RepresentationResolver;
import com.github.ixtf.vertx.RouteRepresentation;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Slf4j
public abstract class JaxRsRouteResolver extends RepresentationResolver<RouteRepresentation> {

    @Override
    protected Stream<? extends RouteRepresentation> resolve() {
        return classes().filter(JaxRs.resourceFilter())
                .collect(toSet()).parallelStream()
                .map(JaxRsResourceRepresentation::new)
                .flatMap(JaxRsResourceRepresentation::routes)
                .peek(it -> log.info("api=" + it.getPath() + "\t address=" + it.getAddress()));
    }

}
