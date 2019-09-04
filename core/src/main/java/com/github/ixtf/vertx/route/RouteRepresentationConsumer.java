package com.github.ixtf.vertx.route;

import com.github.ixtf.vertx.Envelope;
import com.github.ixtf.vertx.Jvertx;
import com.github.ixtf.vertx.apm.Apm;
import com.github.ixtf.vertx.apm.RCTextMapExtractAdapter;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static io.opentracing.propagation.Format.Builtin.TEXT_MAP;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RouteRepresentationConsumer implements Handler<Message<JsonObject>> {
    @EqualsAndHashCode.Include
    private final RouteRepresentation routeRepresentation;
    private final Object proxy;
    private final Function<JsonObject, Object[]> argsFun;
    private final Apm apm;

    private RouteRepresentationConsumer(RouteRepresentation routeRepresentation, Object proxy, Function<JsonObject, Object[]> argsFun) {
        this.routeRepresentation = routeRepresentation;
        this.proxy = proxy;
        this.argsFun = argsFun;
        apm = routeRepresentation.getAnnotation(Apm.class);
    }

    static Handler<Message<JsonObject>> create(RouteRepresentation routeRepresentation, Object proxy, Function<JsonObject, Object[]> argsFun) {
        return new RouteRepresentationConsumer(routeRepresentation, proxy, argsFun);
    }

    @Override
    public void handle(Message<JsonObject> reply) {
        final Span span = initApm(reply);
        Mono.fromCallable(() -> {
            final Object[] args = argsFun.apply(reply.body());
            final Object ret = Jvertx.checkAndInvoke(proxy, routeRepresentation.getMethod(), args);
            return new Envelope(ret);
        }).flatMap(envelope -> {
            final DeliveryOptions deliveryOptions = envelope.getDeliveryOptions();
            return envelope.toMessage().defaultIfEmpty("").map(it -> Pair.of(it, deliveryOptions));
        }).subscribe(pair -> {
            final Object message = pair.getKey();
            final DeliveryOptions deliveryOptions = pair.getValue();
            apmSuccess(span, reply, message, deliveryOptions);
            reply.reply(message, deliveryOptions);
        }, err -> {
            apmError(span, reply, err);
            reply.fail(400, err.getLocalizedMessage());
        });
    }

    private Span initApm(Message<JsonObject> reply) {
        if (apm == null) {
            return null;
        }
        try {
            final String apmService = StringUtils.defaultIfBlank(apm.service(), "worker");
            final Tracer tracer = Jvertx.initTracer(apmService);
            final SpanContext spanContext = tracer.extract(TEXT_MAP, new RCTextMapExtractAdapter(reply));
            final Tracer.SpanBuilder spanBuilder = tracer.buildSpan(routeRepresentation.getPath())
                    .withTag(Tags.COMPONENT, proxy.getClass().getName())
                    .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_CONSUMER)
                    .withTag(Tags.MESSAGE_BUS_DESTINATION, routeRepresentation.getAddress());
            if (spanContext != null) {
                spanBuilder.asChildOf(spanContext);
            }
            return spanBuilder.start();
        } catch (Throwable e) {
            log.error("apm tracer not found", e);
            return null;
        }
    }

    private void apmSuccess(Span span, Message<JsonObject> reply, Object message, DeliveryOptions deliveryOptions) {
        if (span == null) {
            return;
        }
        span.setTag(Tags.ERROR, false);
        span.finish();
    }

    private void apmError(Span span, Message<JsonObject> reply, Throwable err) {
        if (span == null) {
            log.error(String.join(" : ", routeRepresentation.getAddress(), reply.body().encode()), err);
            return;
        }
        span.setTag(Tags.ERROR, true);
        span.log(err.getLocalizedMessage());
        span.finish();
    }

}