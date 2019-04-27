package com.github.ixtf.vertx.api;

import java.util.stream.Stream;

/**
 * @author jzb 2019-02-20
 */
public interface ResourceRepresentation {

    Class getResourceClass();

    Stream<RouteRepresentation> routes();
}
