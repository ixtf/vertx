package com.github.ixtf.vertx.eb;

import io.reactivex.Completable;
import io.vertx.reactivex.core.Vertx;
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

    public Completable consumer(Vertx vertx) {
        return vertx.eventBus().consumer("", reply -> {

        }).rxCompletionHandler();
    }

}