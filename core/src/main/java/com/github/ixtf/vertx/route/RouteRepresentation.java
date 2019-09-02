package com.github.ixtf.vertx.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.Jvertx;
import com.google.common.collect.ImmutableSet;
import com.sun.security.auth.UserPrincipal;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static io.vertx.core.http.HttpMethod.*;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RouteRepresentation {
    @EqualsAndHashCode.Include
    @Getter
    private final HttpMethod httpMethod;
    @EqualsAndHashCode.Include
    @Getter
    private final String path;
    @Getter
    private final String address;
    private final String[] consumes;
    private final String[] produces;
    @Getter
    private final Method method;

    protected RouteRepresentation(HttpMethod httpMethod, String path, String address, String[] consumes, String[] produces, Method method) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.address = address;
        this.consumes = consumes;
        this.produces = produces;
        this.method = method;
    }

    public <T extends Annotation> T getAnnotation(Class<T> clazz) {
        return Optional.ofNullable(method.getAnnotation(clazz)).orElseGet(() -> {
            final Class<?> declaringClass = method.getDeclaringClass();
            return declaringClass.getAnnotation(clazz);
        });
    }

    private String body(JsonObject body) {
        return body.getString("body", null);
    }

    private JsonObject pathParams(JsonObject body) {
        return body.getJsonObject("pathParams");
    }

    private JsonObject queryParams(JsonObject body) {
        return body.getJsonObject("queryParams");
    }

    private Principal defaultPrincipalFun(JsonObject body) {
        final JsonObject jsonObject = body.getJsonObject("principal");
        final String uid = jsonObject.getString("uid");
        return new UserPrincipal(uid);
    }

    public void router(Router router) {
        final Route route = router.route(httpMethod, path);
        if (ArrayUtils.isNotEmpty(consumes)) {
            if (ImmutableSet.of(POST, PUT, PATCH).contains(httpMethod)) {
                Arrays.stream(consumes).forEach(route::consumes);
            }
        }
        if (ArrayUtils.isNotEmpty(produces)) {
            Arrays.stream(produces).forEach(route::produces);
        }
        route.handler(new RouteRepresentationHandler(this));
    }

    public void consumer(Vertx vertx, Function<Class, Object> proxyFun) {
        consumer(vertx, proxyFun.compose(Method::getDeclaringClass).apply(method), this::defaultPrincipalFun);
    }

    public void consumer(Vertx vertx, Object proxy, Function<JsonObject, Principal> principalFun) {
        final RouteRepresentationConsumer handler = new RouteRepresentationConsumer(this, proxy, argsFun(principalFun));
        vertx.eventBus().consumer(address, handler);
    }

    private Function<JsonObject, Object[]> argsFun(Function<JsonObject, Principal> principalFun) {
        final Parameter[] parameters = method.getParameters();
        if (ArrayUtils.isEmpty(parameters)) {
            return it -> new Object[0];
        }
        final Function<JsonObject, ?>[] argFuns = new Function[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            argFuns[i] = argFun(parameters[i], principalFun);
        }
        return jsonObject -> Arrays.stream(argFuns).map(it -> it.apply(jsonObject)).toArray(Object[]::new);
    }

    private Function<JsonObject, ?> argFun(Parameter parameter, Function<JsonObject, Principal> principalFun) {
        final Class<?> parameterType = parameter.getType();
        if (Principal.class.isAssignableFrom(parameterType)) {
            return principalFun;
        }

        final PathParam pathParam = parameter.getAnnotation(PathParam.class);
        if (pathParam != null) {
            final String key = pathParam.value();
            final Function<JsonObject, JsonObject> pathParamsFun = this::pathParams;
            final Function<String, ?> paramFun = Jvertx.paramFun(parameterType);
            return pathParamsFun.andThen(it -> it.getString(key)).andThen(paramFun);
        }

        final QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
        if (queryParam != null) {
            final String defaultValue = getDefaultValue(parameter);
            final String key = queryParam.value();
            final Function<JsonObject, JsonObject> queryParamsFun = this::queryParams;
            final Function<String, ?> paramFun = Jvertx.paramFun(parameterType);
            return queryParamsFun.andThen(queryParams -> Optional.ofNullable(queryParams.getJsonArray(key))
                    .filter(it -> !it.isEmpty())
                    .map(it -> it.getString(0))
                    .filter(J::nonBlank)
                    .orElse(defaultValue))
                    .andThen(paramFun);
        }

        final Function<JsonObject, String> bodyFun = this::body;
        if (String.class == parameterType) {
            return bodyFun;
        }
        if (JsonObject.class == parameterType) {
            return bodyFun.andThen(JsonObject::new);
        }
        if (JsonArray.class == parameterType) {
            return bodyFun.andThen(JsonArray::new);
        }
        if (JsonNode.class == parameterType) {
            return bodyFun.andThen(it -> {
                try {
                    return MAPPER.readValue(it, JsonNode.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return bodyFun.andThen(it -> Jvertx.checkAndGetCommand(parameterType, it));
    }

    private String getDefaultValue(Parameter parameter) {
        return Optional.ofNullable(parameter.getAnnotation(DefaultValue.class))
                .map(DefaultValue::value)
                .orElse(null);
    }

}