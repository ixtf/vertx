package com.github.ixtf.vertx.jax_rs.api;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.Envelope;
import com.github.ixtf.vertx.api.RouteRepresentation;
import com.github.ixtf.vertx.spi.ResourceProvider;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
class RouteRepresentationJaxRs implements RouteRepresentation {
    @Getter
    private final ResourceRepresentationJaxRs resourceRepresentation;
    @Getter
    private final MethodRepresentationJaxRs methodRepresentation;
    @Getter
    private final String jaxRsPath;
    @Getter
    private final String[] jaxRsConsumes;
    @Getter
    private final String[] consumes;
    @Getter
    private final String[] jaxRsProduces;
    @Getter
    private final String[] produces;
    @Getter
    private final HttpMethod httpMethod;
    @Getter
    private final String path;
    @Getter
    private final String address;

    RouteRepresentationJaxRs(ResourceRepresentationJaxRs resourceRepresentation, Method nativeMethod, HttpMethod httpMethod) {
        this.resourceRepresentation = resourceRepresentation;
        this.httpMethod = httpMethod;
        this.methodRepresentation = new MethodRepresentationJaxRs(this, nativeMethod);
        jaxRsConsumes = JaxRs.getConsumes(nativeMethod);
        jaxRsProduces = JaxRs.getProduces(nativeMethod);
        jaxRsPath = JaxRs.getPath(nativeMethod);
        consumes = Optional.ofNullable(jaxRsConsumes)
                .filter(ArrayUtils::isNotEmpty)
                .orElseGet(resourceRepresentation::getJaxRsConsumes);
        produces = Optional.ofNullable(jaxRsProduces)
                .filter(ArrayUtils::isNotEmpty)
                .orElseGet(resourceRepresentation::getJaxRsProduces);
        path = JaxRs.vertxPath(resourceRepresentation.getJaxRsPath(), jaxRsPath);
        address = resourceRepresentation.addressPrefix() + ":" + getHttpMethod().name() + ":" + path;
    }

    @Override
    public Handler<RoutingContext> getRoutingContextHandler() {
        final var messageFun = methodRepresentation.getMessageFun();
        final var optionsFun = methodRepresentation.getDeliveryOptionsFun();
        return rc -> rc.vertx().eventBus().rxSend(address, messageFun.apply(rc), optionsFun.apply(rc)).subscribe(message -> {
            final HttpServerResponse response = rc.response();
            final MultiMap headers = message.headers();
            headers.entries().forEach(it -> response.putHeader(it.getKey(), it.getValue()));
            final Object body = message.body();
            if (body == null) {
                response.end();
                return;
            }
            if (body instanceof String) {
                final String result = (String) body;
                if (J.isBlank(result)) {
                    response.end();
                } else {
                    response.end(result);
                }
                return;
            }
            final byte[] bytes = (byte[]) body;
            response.end(Buffer.buffer(bytes));
        }, rc::fail);
    }

    @Override
    public Handler<Message<Object>> getMessageHandler() {
        final Class<?> returnClass = methodRepresentation.getReturnClass();
        if (CompletionStage.class.isAssignableFrom(returnClass)) {
            return replyTryCatch(reply -> {
                final CompletionStage<?> completionStage = (CompletionStage<?>) invokeMethod(reply);
                completionStage.whenComplete((data, throwable) -> {
                    final Envelope envelope = Envelope.whenComplete(data, throwable);
                    envelope.reply(reply);
                });
            });
        }
        if (Completable.class.isAssignableFrom(returnClass)) {
            return reply -> Single.fromCallable(() -> {
                final Completable completable = (Completable) invokeMethod(reply);
                return completable;
            }).flatMapCompletable(it -> it).subscribe(
                    () -> reply.reply(null),
                    e -> reply.fail(400, e.getLocalizedMessage())
            );
        }
        if (Single.class.isAssignableFrom(returnClass)) {
            return reply -> Single.fromCallable(() -> {
                final Single<?> single = (Single<?>) invokeMethod(reply);
                return single;
            }).flatMap(it -> it).map(Envelope::data).subscribe(
                    it -> it.reply(reply),
                    e -> reply.fail(400, e.getLocalizedMessage())
            );
        }
        if (Maybe.class.isAssignableFrom(returnClass)) {
            return reply -> Single.fromCallable(() -> {
                final Maybe<?> maybe = (Maybe<?>) invokeMethod(reply);
                return maybe;
            }).flatMapMaybe(it -> it).map(Envelope::data).subscribe(
                    it -> it.reply(reply),
                    e -> reply.fail(400, e.getLocalizedMessage())
            );
        }
        if (Flowable.class.isAssignableFrom(returnClass)) {
            return reply -> Single.fromCallable(() -> {
                final Flowable<?> flowable = (Flowable<?>) invokeMethod(reply);
                return flowable;
            }).flatMapPublisher(it -> it).toList().map(Envelope::data).subscribe(
                    it -> it.reply(reply),
                    e -> reply.fail(400, e.getLocalizedMessage())
            );
        }
        if (Future.class.isAssignableFrom(returnClass)) {
            return reply -> Single.fromCallable(() -> {
                final Future<?> future = (Future<?>) invokeMethod(reply);
                return future;
            }).map(future -> {
                if (future.succeeded()) {
                    return Envelope.data(future.result());
                } else {
                    return Envelope.error(future.cause());
                }
            }).subscribe(
                    it -> it.reply(reply),
                    e -> reply.fail(400, e.getLocalizedMessage())
            );
        }
        return reply -> Single.fromCallable(() -> {
            final Object data = invokeMethod(reply);
            return Envelope.data(data);
        }).subscribe(
                it -> it.reply(reply),
                e -> reply.fail(400, e.getLocalizedMessage())
        );
    }

    @SneakyThrows
    private Object invokeMethod(Message<Object> reply) {
        final ResourceProvider provider = resourceRepresentation.getResourceProvider();
        final Class<?> resourceClass = resourceRepresentation.getResourceClass();
        final String methodName = methodRepresentation.getNativeMethod().getName();
        final String body = (String) reply.body();
        final Object[] args = methodRepresentation.getArgsFun().apply(body);
        final Object proxy = provider.getProxy(resourceClass);
        return MethodUtils.invokeMethod(proxy, methodName, args);
    }

    private Handler<Message<Object>> replyTryCatch(Consumer<Message<Object>> consumer) {
        return reply -> {
            try {
                consumer.accept(reply);
            } catch (Exception e) {
                log.error("", e);
                reply.fail(400, e.getLocalizedMessage());
            }
        };
    }

    @Override
    public Stream<String> consumes() {
        return Arrays.stream(consumes);
    }

    @Override
    public Stream<String> produces() {
        return Arrays.stream(produces);
    }

}