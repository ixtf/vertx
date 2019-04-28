package com.github.ixtf.vertx.route;

import com.github.ixtf.vertx.Jvertx;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class RouteEBRepresentation extends RouteRepresentation {
    private final Object proxy;
    private final Function<RCEnvelope, Object[]> argsFun;

    private RouteEBRepresentation(Method method, HttpMethod httpMethod, String path, String[] consumes, String[] produces, String address, Object proxy) {
        super(method, httpMethod, path, consumes, produces, address);
        this.proxy = proxy;

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
    }

    protected RouteEBRepresentation(RouteRepresentation route, Object proxy) {
        this(route.method, route.httpMethod, route.path, route.consumes, route.produces, route.address, proxy);
    }

    public void consumer(Vertx vertx) {
        vertx.eventBus().<JsonObject>consumer(address, reply -> Single.just(reply.body()).map(RCEnvelope::new).map(envelope -> {
            final Object[] args = argsFun.apply(envelope);
            final Object ret = Jvertx.checkAndInvoke(proxy, method, args);
            return RCReplyEnvelope.create(reply, envelope, ret);
        }).subscribe(RCReplyEnvelope::reply, err -> {
            log.error("", err);
            reply.fail(400, err.getLocalizedMessage());
        }));
    }

}