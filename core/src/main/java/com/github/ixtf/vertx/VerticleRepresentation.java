package com.github.ixtf.vertx;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

/**
 * @author jzb 2019-02-14
 */
public interface VerticleRepresentation {
    Future<String> deploy(Vertx vertx);
}