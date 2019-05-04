package com.github.ixtf.vertx;

import com.github.ixtf.japp.core.Constant;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.japp.core.exception.JError;
import com.github.ixtf.vertx.util.CorsConfig;
import com.github.ixtf.vertx.util.RepresentationResolver;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.*;
import io.vertx.reactivex.ext.web.sstore.SessionStore;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.*;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.github.ixtf.japp.core.Constant.YAML_MAPPER;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-02-16
 */
public final class Jvertx {
    public static final String API = "API";
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
        });
    }

    public static void enableCommon(Router router) {
        router.route().handler(BodyHandler.create());
        router.route().handler(ResponseContentTypeHandler.create());
        router.route().handler(CookieHandler.create());
    }

    public static void enableCommon(Router router, SessionStore sessionStore) {
        enableCommon(router);
        router.route().handler(SessionHandler.create(sessionStore));
    }

    public static void enableCors(Router router, CorsConfig corsConfig) {
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
                .allowedHeaders(allowedHeaders)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.PATCH)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.HEAD)
                .allowedMethod(HttpMethod.OPTIONS));
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
    public static JsonObject readJsonObject(String first, String... more) {
        return readJsonObject(Paths.get(first, more));
    }

    @SneakyThrows
    public static JsonObject readJsonObject(Path path) {
        final String extension = FilenameUtils.getExtension(path.toString());
        switch (StringUtils.lowerCase(extension)) {
            case "json": {
                final Map map = MAPPER.readValue(path.toFile(), Map.class);
                return new JsonObject(map);
            }
            case "yaml":
            case "yml": {
                final Map map = YAML_MAPPER.readValue(path.toFile(), Map.class);
                return new JsonObject(map);
            }
        }
        throw new RuntimeException(path + "，格式不支持！");
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
        if (violations.size() == 0) {
            return command;
        }
        throw new ConstraintViolationException(violations);
    }

    public static Object checkAndInvoke(Object proxy, Method method, Object[] args) throws Exception {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final Validator validator = validatorFactory.getValidator();
        final ExecutableValidator executableValidator = validator.forExecutables();
        final Set<ConstraintViolation<Object>> violations = executableValidator.validateParameters(proxy, method, args);
        if (violations.size() == 0) {
            try {
                return method.invoke(proxy, args);
            } catch (IllegalAccessException e) {
                throw e;
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getTargetException());
            }
        }
        throw new ConstraintViolationException(violations);
    }

    private Jvertx() {
    }
}
