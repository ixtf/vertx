package com.github.ixtf.vertx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.ixtf.japp.core.Constant;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.core.exception.JError;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

import javax.validation.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2019-02-16
 */
public final class Jvertx {
    public static final String API = "API";

    private static final LoadingCache<Class<? extends RepresentationResolver>, Collection<?>> RESOLVER_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(resolverClass -> {
                final RepresentationResolver<?> resolver = resolverClass.getDeclaredConstructor().newInstance();
                return resolver.resolve();
            });

    public static <T> Stream<T> resolve(Class<? extends RepresentationResolver<T>>... classes) {
        return Arrays.stream(classes).parallel().flatMap(it -> {
            final Collection<T> collection = (Collection<T>) RESOLVER_CACHE.get(it);
            return collection.parallelStream();
        }).distinct();
    }

    public static void failureHandler(RoutingContext rc) {
        final HttpServerResponse response = rc.response().setStatusCode(400);
        final Throwable failure = rc.failure();
        final JsonObject result = new JsonObject();
        // todo response content-type 为 json
        if (failure instanceof JError) {
            final JError ex = (JError) failure;
            result.put("errorCode", ex.getErrorCode())
                    .put("errorMessage", ex.getMessage());
        } else {
            result.put("errorCode", Constant.ErrorCode.SYSTEM)
                    .put("errorMessage", failure.getMessage());
        }
        response.end(result.encode());
    }

    @SneakyThrows(JsonProcessingException.class)
    public static <T> T checkAndGetCommand(Class<T> clazz, String json) {
        final T command = MAPPER.readValue(json, clazz);
        return checkAndGetCommand(command);
    }

    public static <T> T checkAndGetCommand(T command) {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final Validator validator = validatorFactory.getValidator();
        final Set<ConstraintViolation<Object>> violations = validator.validate(command);
        if (J.nonEmpty(violations)) {
            throw new ConstraintViolationException(violations);
        }
        return command;
    }

    public static Router router(Vertx vertx, CorsConfig corsConfig) {
        return router(vertx, corsConfig, Jvertx::failureHandler);
    }

    public static <T> Mono<T> mono(Future<T> future) {
        return Mono.create(monoSink -> future.setHandler(it -> {
            if (it.succeeded()) {
                final T result = it.result();
                if (result == null) {
                    monoSink.success();
                } else {
                    monoSink.success(result);
                }
            } else {
                monoSink.error(it.cause());
            }
        }));
    }

    public static Router router(Vertx vertx, CorsConfig corsConfig, Handler<RoutingContext> failureHandler) {
        final Router router = Router.router(vertx);
        router.route().handler(ResponseContentTypeHandler.create());
        final String domainP = Stream.concat(
                Stream.of("localhost", "127\\.0\\.0\\.1").parallel(),
                J.emptyIfNull(corsConfig.getDomainPatterns()).parallelStream()
        ).collect(joining("|"));
        final Set<String> allowedHeaders = Stream.concat(
                Stream.of("authorization", "origin", "accept", "content-type", "access-control-allow-origin").parallel(),
                J.emptyIfNull(corsConfig.getAllowedHeaders()).parallelStream()
        ).collect(toSet());
        router.route().handler(CorsHandler.create("^http(s)?://(" + domainP + ")(:[1-9]\\d+)?")
                .allowCredentials(corsConfig.isAllowCredentials())
                .allowedMethods(Set.of(HttpMethod.values()))
                .allowedHeaders(allowedHeaders));
        router.route().failureHandler(failureHandler);
        return router;
    }

}
