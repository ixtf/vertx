package com.github.ixtf.vertx.route;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.Envelope;
import com.github.ixtf.vertx.JvertxOptions;
import com.github.ixtf.vertx.apm.RCTextMapExtractAdapter;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Mono;

import javax.validation.*;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static io.opentracing.propagation.Format.Builtin.TEXT_MAP;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class RouteRepresentationConsumer extends AbstractVerticle implements Handler<Message<JsonObject>> {
    @EqualsAndHashCode.Include
    private final RouteRepresentation routeRepresentation;
    private final Function<Class, Object> proxyFun;
    private final Object proxy;
    private final Function<JsonObject, Object[]> argsFun;
    private final JvertxOptions jvertxOptions;

    RouteRepresentationConsumer(RouteRepresentation routeRepresentation, Function<Class, Object> proxyFun, Function<JsonObject, Principal> principalFun) {
        this.routeRepresentation = routeRepresentation;
        this.proxyFun = proxyFun;
        this.argsFun = RoutingContextEnvelope.argsFun(routeRepresentation.getMethod(), principalFun);
        proxy = proxyFun.compose(Method::getDeclaringClass).apply(routeRepresentation.getMethod());
        jvertxOptions = routeRepresentation.getAnnotation(JvertxOptions.class);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        vertx.eventBus().consumer(routeRepresentation.getAddress(), this).completionHandler(startFuture);
    }

    @SneakyThrows
    public static Object checkAndInvoke(Object proxy, Method method, Object[] args) {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final Validator validator = validatorFactory.getValidator();
        final ExecutableValidator executableValidator = validator.forExecutables();
        final Set<ConstraintViolation<Object>> violations = executableValidator.validateParameters(proxy, method, args);
        if (J.nonEmpty(violations)) {
            throw new ConstraintViolationException(violations);
        }
        return MethodUtils.invokeMethod(proxy, true, method.getName(), args);
    }

    @Override
    public void handle(Message<JsonObject> reply) {
        final Span span = initApm(reply);
        Mono.fromCallable(() -> {
            final Object[] args = argsFun.apply(reply.body());
            final Object ret = checkAndInvoke(proxy, routeRepresentation.getMethod(), args);
            return new Envelope(ret);
        }).flatMap(envelope -> {
            final DeliveryOptions deliveryOptions = envelope.getDeliveryOptions();
            return envelope.toMessage().defaultIfEmpty("").map(it -> Pair.of(it, deliveryOptions));
        }).subscribe(pair -> {
            final DeliveryOptions deliveryOptions = pair.getValue();
            final Object message = pair.getKey();
            apmSuccess(span, reply, message, deliveryOptions);
            reply.reply(message, deliveryOptions);
        }, err -> {
            apmError(span, reply, err);
            reply.fail(400, err.getLocalizedMessage());
        });
    }

    private Span initApm(Message<JsonObject> reply) {
        try {
            return Optional.ofNullable(jvertxOptions)
                    .filter(JvertxOptions::apm)
                    .map(service -> {
                        final Tracer tracer = (Tracer) proxyFun.apply(Tracer.class);
                        final SpanContext spanContext = tracer.extract(TEXT_MAP, new RCTextMapExtractAdapter(reply));
                        final Tracer.SpanBuilder spanBuilder = tracer.buildSpan(routeRepresentation.getPath())
                                .withTag(Tags.COMPONENT, proxy.getClass().getName())
                                .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_CONSUMER)
                                .withTag(Tags.MESSAGE_BUS_DESTINATION, routeRepresentation.getAddress());
                        if (spanContext != null) {
                            spanBuilder.asChildOf(spanContext);
                        }
                        return spanBuilder.start();
                    })
                    .orElse(null);
        } catch (Throwable err) {
            log.error("apm tracer not found", err);
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
            return;
        }
        span.setTag(Tags.ERROR, true);
        span.log(err.getLocalizedMessage());
        span.finish();
    }

}