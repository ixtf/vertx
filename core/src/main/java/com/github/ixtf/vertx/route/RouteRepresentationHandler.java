package com.github.ixtf.vertx.route;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.JvertxOptions;
import com.github.ixtf.vertx.apm.RCTextMapExtractAdapter;
import com.github.ixtf.vertx.apm.RCTextMapInjectAdapter;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Function;

import static io.opentracing.propagation.Format.Builtin.TEXT_MAP;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RouteRepresentationHandler implements Handler<RoutingContext> {
    private final RouteRepresentation routeRepresentation;
    private final JvertxOptions jvertxOptions;
    private final Function<Class, Object> proxyFun;

    RouteRepresentationHandler(RouteRepresentation routeRepresentation, Function<Class, Object> proxyFun) {
        this.routeRepresentation = routeRepresentation;
        this.proxyFun = proxyFun;
        jvertxOptions = routeRepresentation.getAnnotation(JvertxOptions.class);
    }

    private DeliveryOptions getDeliveryOptions() {
        final DeliveryOptions deliveryOptions = new DeliveryOptions();
        if (jvertxOptions != null) {
            deliveryOptions.setSendTimeout(jvertxOptions.timeout());
        }
        return deliveryOptions;
    }

    @Override
    public void handle(RoutingContext rc) {
        final HttpServerResponse response = rc.response();
        final DeliveryOptions deliveryOptions = getDeliveryOptions();
        final Object message = RoutingContextEnvelope.encode(rc);
        final Span span = initApm(rc, deliveryOptions);
        rc.vertx().eventBus().request(routeRepresentation.getAddress(), message, deliveryOptions, ar -> {
            try {
                if (ar.succeeded()) {
                    final Message<Object> reply = ar.result();
                    final MultiMap headers = reply.headers();
                    headers.entries().forEach(it -> response.putHeader(it.getKey(), it.getValue()));
                    apmSuccess(span, rc, reply);
                    rc.response().end(buffer(reply));
                } else {
                    apmError(span, rc, ar.cause());
                    rc.fail(ar.cause());
                }
            } catch (Throwable e) {
                apmError(span, rc, e);
                rc.fail(e);
            }
        });
    }

    private Buffer buffer(Message<Object> reply) {
        final Object body = reply.body();
        if (body == null) {
            return Buffer.buffer();
        }
        if (body instanceof String) {
            final String result = (String) body;
            if (J.isBlank(result)) {
                return Buffer.buffer();
            } else {
                return Buffer.buffer(result);
            }
        }
        if (body instanceof JsonObject) {
            final JsonObject jsonObject = (JsonObject) body;
            return Buffer.buffer(jsonObject.encode());
        }
        if (body instanceof JsonArray) {
            final JsonArray jsonArray = (JsonArray) body;
            return Buffer.buffer(jsonArray.encode());
        }
        final byte[] bytes = (byte[]) body;
        return Buffer.buffer(bytes);
    }

    private Span initApm(RoutingContext rc, DeliveryOptions deliveryOptions) {
        try {
            return Optional.ofNullable(jvertxOptions)
                    .map(JvertxOptions::apmService)
                    .filter(J::nonBlank)
                    .map(service -> {
                        final Tracer tracer = (Tracer) proxyFun.apply(Tracer.class);
                        final HttpServerRequest request = rc.request();
                        final Tracer.SpanBuilder spanBuilder = tracer.buildSpan(routeRepresentation.getPath())
                                .withTag(Tags.HTTP_METHOD, request.rawMethod())
                                .withTag(Tags.HTTP_URL, request.absoluteURI())
                                .withTag(Tags.COMPONENT, routeRepresentation.getMethod().getDeclaringClass().getName())
                                .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_PRODUCER)
                                .withTag(Tags.MESSAGE_BUS_DESTINATION, routeRepresentation.getAddress());
                        final SpanContext spanContext = tracer.extract(TEXT_MAP, new RCTextMapExtractAdapter(rc));
                        if (spanContext != null) {
                            spanBuilder.asChildOf(spanContext);
                        }
                        final Span span = spanBuilder.start();
                        tracer.inject(span.context(), TEXT_MAP, new RCTextMapInjectAdapter(deliveryOptions));
                        return span;
                    })
                    .orElse(null);
        } catch (Throwable err) {
            log.error("apm tracer not found", err);
            return null;
        }
    }

    private void apmSuccess(Span span, RoutingContext rc, Message<Object> reply) {
        if (span == null) {
            return;
        }
        span.setTag(Tags.HTTP_STATUS, 200);
        span.setTag(Tags.ERROR, false);
        span.finish();
    }

    private void apmError(Span span, RoutingContext rc, Throwable err) {
        if (span == null) {
            return;
        }
        span.setTag(Tags.HTTP_STATUS, 400);
        span.setTag(Tags.ERROR, true);
        span.log(err.getMessage());
        span.finish();
    }

}