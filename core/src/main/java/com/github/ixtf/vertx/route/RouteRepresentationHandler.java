package com.github.ixtf.vertx.route;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.Jvertx;
import com.github.ixtf.vertx.VertxDelivery;
import com.github.ixtf.vertx.apm.Apm;
import com.github.ixtf.vertx.apm.RCTextMapInjectAdapter;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static io.opentracing.propagation.Format.Builtin.TEXT_MAP;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RouteRepresentationHandler implements Handler<RoutingContext> {
    private final RouteRepresentation routeRepresentation;
    private final VertxDelivery vertxDelivery;
    private final Apm apm;

    RouteRepresentationHandler(RouteRepresentation routeRepresentation) {
        this.routeRepresentation = routeRepresentation;
        vertxDelivery = routeRepresentation.getAnnotation(VertxDelivery.class);
        apm = routeRepresentation.getAnnotation(Apm.class);
    }

    private DeliveryOptions getDeliveryOptions() {
        final DeliveryOptions deliveryOptions = new DeliveryOptions();
        if (vertxDelivery != null) {
            deliveryOptions.setSendTimeout(vertxDelivery.timeout());
        }
        return deliveryOptions;
    }

    @Override
    public void handle(RoutingContext rc) {
        final HttpServerResponse response = rc.response();
        final DeliveryOptions deliveryOptions = getDeliveryOptions();
        final Object message = Jvertx.encode(rc);
        final Span span = initApm(rc, deliveryOptions);
        rc.vertx().eventBus().send(routeRepresentation.getAddress(), message, deliveryOptions, ar -> {
            if (ar.succeeded()) {
                final Message<Object> reply = ar.result();
                final MultiMap headers = reply.headers();
                headers.entries().forEach(it -> response.putHeader(it.getKey(), it.getValue()));
                rc.response().end(buffer(reply));
                apmSuccess(span, rc, reply);
            } else {
                rc.fail(ar.cause());
                apmError(span, rc, ar.cause());
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
        final byte[] bytes = (byte[]) body;
        return Buffer.buffer(bytes);
    }

    private Span initApm(RoutingContext rc, DeliveryOptions deliveryOptions) {
        if (apm == null) {
            return null;
        }
        try {
            final String apmService = StringUtils.defaultIfBlank(apm.service(), "agent");
            final HttpServerRequest request = rc.request();
            final Tracer tracer = Jvertx.initTracer(apmService);
            final Span span = tracer.buildSpan(routeRepresentation.getPath())
                    .withTag(Tags.HTTP_METHOD, request.rawMethod())
                    .withTag(Tags.HTTP_URL, request.absoluteURI())
                    .withTag(Tags.COMPONENT, routeRepresentation.getMethod().getDeclaringClass().getName())
                    .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_PRODUCER)
                    .withTag(Tags.MESSAGE_BUS_DESTINATION, routeRepresentation.getAddress())
                    .start();
            tracer.inject(span.context(), TEXT_MAP, new RCTextMapInjectAdapter(deliveryOptions));
            return span;
        } catch (Throwable e) {
            log.error("apm tracer not found", e);
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
        span.log(err.getLocalizedMessage());
        span.finish();
    }

}