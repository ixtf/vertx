package com.github.ixtf.vertx.api;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.util.stream.Stream;

/**
 * @author jzb 2019-02-14
 */
public interface RouteRepresentation {

    String getPath();

    HttpMethod getHttpMethod();

    Stream<String> consumes();

    Stream<String> produces();

    Handler<RoutingContext> getRoutingContextHandler();

    String getAddress();

    Handler<Message<Object>> getMessageHandler();

}