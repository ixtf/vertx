package com.github.ixtf.vertx;

import com.github.ixtf.japp.core.Constant;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.core.exception.JError;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import lombok.SneakyThrows;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.validation.*;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-02-16
 */
public final class Jvertx {
    public static final String API = "API";

    public static final JsonObject encode(RoutingContext rc) {
        final JsonObject principal = Optional.ofNullable(rc.user()).map(User::principal).orElse(null);
        final Map<String, String> pathParams = rc.pathParams();
        final Map<String, List<String>> queryParams = rc.queryParams().names().parallelStream().collect(toMap(Function.identity(), rc.queryParams()::getAll));
        return new JsonObject().put("principal", principal)
                .put("pathParams", pathParams)
                .put("queryParams", queryParams)
                .put("body", rc.getBodyAsString());
    }

    private static final LoadingCache<Class<? extends RepresentationResolver>, Collection<?>> RESOLVER_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public Collection<?> load(Class<? extends RepresentationResolver> resolverClass) throws Exception {
                    final RepresentationResolver<?> resolver = resolverClass.getDeclaredConstructor().newInstance();
                    return resolver.resolve().collect(toList());
                }
            });

    public static <T> Stream<T> resolve(Class<? extends RepresentationResolver<T>>... classes) {
        return Arrays.stream(classes).parallel().flatMap(it -> {
            try {
                final Collection<T> collection = (Collection<T>) RESOLVER_CACHE.get(it);
                return collection.parallelStream();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
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
                    .put("errorMessage", failure.getLocalizedMessage());
        }
        response.end(result.encode());
    }

    @SneakyThrows
    public static <T> T checkAndGetCommand(Class<T> clazz, String json) {
        final T command = MAPPER.readValue(json, clazz);
        return checkAndGetCommand(command);
    }

    @SneakyThrows
    public static <T> T checkAndGetCommand(T command) {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final Validator validator = validatorFactory.getValidator();
        final Set<ConstraintViolation<Object>> violations = validator.validate(command);
        if (J.nonEmpty(violations)) {
            throw new ConstraintViolationException(violations);
        }
        return command;
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

    private Jvertx() {
    }

    public static Tracer initTracer(String service) {
        io.jaegertracing.Configuration.SamplerConfiguration samplerConfig = io.jaegertracing.Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1);
        io.jaegertracing.Configuration.ReporterConfiguration reporterConfig = io.jaegertracing.Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
        io.jaegertracing.Configuration config = new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }

    public static Function<String, ?> paramFun(Class<?> parameterType) {
        if (String.class.isAssignableFrom(parameterType)) {
            return Function.identity();
        }

        if (boolean.class.isAssignableFrom(parameterType)) {
            return BooleanUtils::toBoolean;
        }
        if (Boolean.class.isAssignableFrom(parameterType)) {
            return BooleanUtils::toBooleanObject;
        }

        if (int.class.isAssignableFrom(parameterType)) {
            return NumberUtils::toInt;
        }
        if (Integer.class.isAssignableFrom(parameterType)) {
            return NumberUtils::createInteger;
        }

        if (float.class.isAssignableFrom(parameterType)) {
            return NumberUtils::toFloat;
        }
        if (Float.class.isAssignableFrom(parameterType)) {
            return NumberUtils::createFloat;
        }

        if (double.class.isAssignableFrom(parameterType)) {
            return NumberUtils::toDouble;
        }
        if (Double.class.isAssignableFrom(parameterType)) {
            return NumberUtils::createDouble;
        }

        if (short.class.isAssignableFrom(parameterType)) {
            return NumberUtils::toShort;
        }
        if (Short.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Short.valueOf(it);
        }

        if (byte.class.isAssignableFrom(parameterType)) {
            return NumberUtils::toByte;
        }
        if (Byte.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Byte.valueOf(it);
        }

        throw new UnsupportedOperationException();
    }

    public static Router router(Vertx vertx, CorsConfig corsConfig) {
        return router(vertx, corsConfig, Jvertx::failureHandler);
    }

    public static Router router(Vertx vertx, CorsConfig corsConfig, Handler<RoutingContext> failureHandler) {
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(ResponseContentTypeHandler.create());
        router.route().handler(CookieHandler.create());
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
