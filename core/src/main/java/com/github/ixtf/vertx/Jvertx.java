package com.github.ixtf.vertx;

import com.github.ixtf.japp.core.Constant;
import com.github.ixtf.japp.core.exception.JException;
import com.github.ixtf.japp.core.exception.JMultiException;
import com.github.ixtf.vertx.api.ResourceRepresentation;
import com.github.ixtf.vertx.api.RouteRepresentation;
import com.github.ixtf.vertx.spi.ResourceProvider;
import com.google.common.collect.Sets;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.*;
import io.vertx.reactivex.ext.web.sstore.SessionStore;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-02-16
 */
public final class Jvertx {

    public static void enableCommon(Router router) {
        router.route().handler(BodyHandler.create());
        router.route().handler(ResponseContentTypeHandler.create());
        router.route().handler(CookieHandler.create());
    }

    public static void enableCommon(Router router, SessionStore sessionStore) {
        enableCommon(router);
        router.route().handler(SessionHandler.create(sessionStore));
    }

    public static void enableCors(Router router, Set<String> domains) {
        final Collection<String> patterns = Sets.newHashSet("localhost", "127\\.0\\.0\\.1");
        patterns.addAll(domains);
        final String domainP = patterns.stream().collect(Collectors.joining("|"));
        router.route().handler(CorsHandler.create("^http(s)?://(" + domainP + ")(:[1-9]\\d+)?")
                .allowCredentials(false)
                .allowedHeader("x-requested-with")
                .allowedHeader("access-control-allow-origin")
                .allowedHeader("origin")
                .allowedHeader("content-type")
                .allowedHeader("accept")
                .allowedHeader("authorization")
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
        if (failure instanceof JMultiException) {
            final JMultiException ex = (JMultiException) failure;
            final JsonArray errors = new JsonArray();
            result.put("errorCode", Constant.ErrorCode.MULTI)
                    .put("errors", errors);
            ex.getExceptions().forEach(it -> {
                final JsonObject error = new JsonObject()
                        .put("errorCode", it.getErrorCode())
                        .put("errorMessage", it.getMessage());
                errors.add(error);
            });
        } else if (failure instanceof JException) {
            final JException ex = (JException) failure;
            result.put("errorCode", ex.getErrorCode())
                    .put("errorMessage", ex.getMessage());
        } else {
            result.put("errorCode", Constant.ErrorCode.SYSTEM)
                    .put("errorMessage", failure.getLocalizedMessage());
        }
        response.end(result.encode());
    }

    public static <T> T readCommand(Class<T> clazz, String json) throws IOException, JException {
        final T command = MAPPER.readValue(json, clazz);
        return checkCommand(command);
    }

    public static <T> T checkCommand(T command) throws JException {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        final Validator validator = validatorFactory.getValidator();
        final Set<ConstraintViolation<T>> violations = validator.validate(command);
        if (violations.size() == 0) {
            return command;
        }
        final List<JException> exceptions = violations.stream().map(violation -> {
            final String propertyPath = violation.getPropertyPath().toString();
            return new JException(Constant.ErrorCode.SYSTEM, propertyPath + ":" + violation.getMessage());
        }).collect(toList());
        if (violations.size() == 1) {
            throw exceptions.get(0);
        }
        throw new JMultiException(exceptions);
    }

    public static Stream<RouteRepresentation> routes() {
        return resources().flatMap(ResourceRepresentation::routes);
    }

    public static Stream<ResourceRepresentation> resources() {
        return Holder.RESOURCE_PROVIDERS.list().flatMap(ResourceProvider::listResources);
    }

    private static class Holder {
        private static final RouteProviderProviderRegistry RESOURCE_PROVIDERS = new RouteProviderProviderRegistry();
    }

    private static class RouteProviderProviderRegistry {
        private final List<ResourceProvider> resourceProviders;

        private RouteProviderProviderRegistry() {
            resourceProviders = StreamSupport.stream(ServiceLoader.load(ResourceProvider.class).spliterator(), false)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }

        private Stream<ResourceProvider> list() {
            return resourceProviders.stream();
        }
    }

    private Jvertx() {
    }
}
