package com.github.ixtf.vertx.route;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.Envelope;
import com.github.ixtf.vertx.JvertxOptions;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
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
import java.util.Set;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RouteRepresentationConsumer implements Handler<Message<JsonObject>> {
    @EqualsAndHashCode.Include
    private final RouteRepresentation routeRepresentation;
    private final Function<Class, Object> proxyFun;
    private final Object proxy;
    private final Function<JsonObject, Object[]> argsFun;
    private final JvertxOptions jvertxOptions;

    RouteRepresentationConsumer(RouteRepresentation routeRepresentation, Function<Class, Object> proxyFun, Function<JsonObject, Object[]> argsFun) {
        this.routeRepresentation = routeRepresentation;
        this.proxyFun = proxyFun;
        this.argsFun = argsFun;
        proxy = proxyFun.compose(Method::getDeclaringClass).apply(routeRepresentation.getMethod());
        jvertxOptions = routeRepresentation.getAnnotation(JvertxOptions.class);
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
            reply.reply(message, deliveryOptions);
            apmSuccess(span, reply, message, deliveryOptions);
        }, err -> {
            reply.fail(400, err.getLocalizedMessage());
            apmError(span, reply, err);
        });
    }

    private Span initApm(Message<JsonObject> reply) {
        return null;

//        if (apm == null) {
//            return null;
//        }
//        try {
//            final String apmService = StringUtils.defaultIfBlank(apm.service(), "worker");
//            final Tracer tracer = Jvertx.initTracer(apmService);
//            final SpanContext spanContext = tracer.extract(TEXT_MAP, new RCTextMapExtractAdapter(reply));
//            final Tracer.SpanBuilder spanBuilder = tracer.buildSpan(routeRepresentation.getPath())
//                    .withTag(Tags.COMPONENT, proxy.getClass().getName())
//                    .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_CONSUMER)
//                    .withTag(Tags.MESSAGE_BUS_DESTINATION, routeRepresentation.getAddress());
//            if (spanContext != null) {
//                spanBuilder.asChildOf(spanContext);
//            }
//            return spanBuilder.start();
//        } catch (Throwable e) {
//            log.error("apm tracer not found", e);
//            return null;
//        }
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