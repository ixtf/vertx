package com.github.ixtf.vertx;

import io.reactivex.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.Vertx;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
public class RouteEBRepresentation extends RouteRepresentation {
    private final Object proxy;
    private final Method method;
    private final Function<RCEnvelope, Object[]> argsFun;
    private final Function<Object, Future<RCReplyEnvelope>> invokeReturnFun;

    protected RouteEBRepresentation(HttpMethod httpMethod, String path, String[] consumes, String[] produces, String address, Object proxy, Method method) {
        super(httpMethod, path, consumes, produces, address);
        this.proxy = proxy;
        this.method = method;

        final Parameter[] parameters = method.getParameters();
        if (parameters == null || parameters.length == 0) {
            argsFun = envelope -> new Object[0];
        } else {
            final Function<RCEnvelope, ? extends Object>[] argFuns = new Function[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                argFuns[i] = RCEnvelope.argFun(parameters[i]);
            }
            argsFun = envelope -> Arrays.stream(argFuns).map(it -> it.apply(envelope)).toArray(Object[]::new);
        }

        invokeReturnFun = invokeReturnFun(method.getReturnType());
    }

    protected RouteEBRepresentation(RouteRepresentation routeRepresentation, Object proxy, Method method) {
        this(routeRepresentation.httpMethod, routeRepresentation.path, routeRepresentation.consumes, routeRepresentation.produces, routeRepresentation.address, proxy, method);
    }

    private Function<Object, Future<RCReplyEnvelope>> invokeReturnFun(Class<?> returnType) {
        if (Completable.class.isAssignableFrom(returnType)) {
            return it -> {
                final Future<RCReplyEnvelope> future = Future.future();
                final Completable completable = (Completable) it;
                completable.subscribe(() -> future.complete(RCReplyEnvelope.data(null)), future::fail);
                return future;
            };
        }
        if (Maybe.class.isAssignableFrom(returnType)) {
            return it -> {
                final Future<RCReplyEnvelope> future = Future.future();
                final Maybe<Object> maybe = (Maybe) it;
                maybe.toSingle().map(RCReplyEnvelope::data).subscribe(data -> future.complete(data), future::fail);
                return future;
            };
        }
        if (Flowable.class.isAssignableFrom(returnType)) {
            return it -> {
                final Future<RCReplyEnvelope> future = Future.future();
                final Flowable<Object> flowable = (Flowable) it;
                flowable.toList().map(RCReplyEnvelope::data).subscribe(data -> future.complete(data), future::fail);
                return future;
            };
        }
        if (Single.class.isAssignableFrom(returnType)) {
            return it -> {
                final Future<RCReplyEnvelope> future = Future.future();
                final Single<Object> single = (Single) it;
                single.map(RCReplyEnvelope::data).subscribe(data -> future.complete(data), future::fail);
                return future;
            };
        }
        if (Observable.class.isAssignableFrom(returnType)) {
            return it -> {
                final Future<RCReplyEnvelope> future = Future.future();
                final Observable<Object> observable = (Observable) it;
                observable.map(RCReplyEnvelope::data).subscribe(data -> future.complete(data), future::fail);
                return future;
            };
        }
        if (CompletionStage.class.isAssignableFrom(returnType)) {
            return it -> {
                final Future<RCReplyEnvelope> future = Future.future();
                final CompletionStage<Object> completionStage = (CompletionStage) it;
                completionStage.thenApply(RCReplyEnvelope::data).whenComplete((data, err) -> {
                    if (err == null) {
                        future.complete(data);
                    } else {
                        future.fail(err);
                    }
                });
                return future;
            };
        }
        if (java.util.concurrent.Future.class.isAssignableFrom(returnType)) {
            return it -> {
                final Future<RCReplyEnvelope> future = Future.future();
                try {
                    final java.util.concurrent.Future<Object> f = (java.util.concurrent.Future) it;
                    final RCReplyEnvelope data = RCReplyEnvelope.data(f.get());
                    future.complete(data);
                } catch (Throwable err) {
                    future.fail(err);
                }
                return future;
            };
        }
        return data -> Future.succeededFuture(RCReplyEnvelope.data(data));
    }

    public void consumer(Vertx vertx) {
        vertx.eventBus().<JsonObject>consumer(address, reply -> Single.just(reply.body())
                .map(RCEnvelope::new)
                .map(envelope -> {
                    final Object[] args = argsFun.apply(envelope);
                    final Object data = Jvertx.checkAndInvoke(proxy, method, args);
                    return invokeReturnFun.apply(data);
                })
                .subscribe(f -> {
                    if (f.failed()) {
                        final String message = Optional.ofNullable(f.cause())
                                .map(Throwable::getLocalizedMessage)
                                .orElse(null);
                        reply.fail(400, message);
                    } else {
                        f.result().reply(reply);
                    }
                }, err -> reply.fail(400, err.getLocalizedMessage())));
    }

}