package com.github.ixtf.vertx.eb;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EBRepresentation {
    private final Object proxy;
    private final Method method;

    public EBRepresentation(Object proxy, Method method) {
        this.proxy = proxy;
        this.method = method;
    }

    public void consumer(Vertx vertx, Handler<AsyncResult<Void>> handler) {
        vertx.eventBus().consumer("", reply -> {

        }).completionHandler(handler);
    }

}