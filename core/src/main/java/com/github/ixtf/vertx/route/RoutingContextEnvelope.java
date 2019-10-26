package com.github.ixtf.vertx.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.Jvertx;
import com.sun.security.auth.UserPrincipal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toMap;

/**
 * @author jzb 2019-10-15
 */
public class RoutingContextEnvelope {

    public static final JsonObject encode(RoutingContext rc) {
        final JsonObject principal = Optional.ofNullable(rc.user()).map(User::principal).orElse(null);
        final Map<String, String> pathParams = rc.pathParams();
        final Map<String, List<String>> queryParams = rc.queryParams().names().parallelStream().collect(toMap(Function.identity(), rc.queryParams()::getAll));
        return new JsonObject().put("principal", principal)
                .put("pathParams", pathParams)
                .put("queryParams", queryParams)
                .put("body", rc.getBodyAsString());
    }

    private static String body(JsonObject body) {
        return body.getString("body", null);
    }

    private static JsonObject pathParams(JsonObject body) {
        return body.getJsonObject("pathParams");
    }

    private static JsonObject queryParams(JsonObject body) {
        return body.getJsonObject("queryParams");
    }

    public static Principal defaultPrincipalFun(JsonObject body) {
        final JsonObject jsonObject = body.getJsonObject("principal");
        final String uid = jsonObject.getString("uid");
        return new UserPrincipal(uid);
    }

    public static Function<JsonObject, Object[]> argsFun(Method method, Function<JsonObject, Principal> principalFun) {
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

    private static Function<JsonObject, ?> argFun(Parameter parameter, Function<JsonObject, Principal> principalFun) {
        final Class<?> parameterType = parameter.getType();
        if (Principal.class.isAssignableFrom(parameterType)) {
            return principalFun;
        }

        final PathParam pathParam = parameter.getAnnotation(PathParam.class);
        if (pathParam != null) {
            final String key = pathParam.value();
            final Function<JsonObject, JsonObject> pathParamsFun = RoutingContextEnvelope::pathParams;
            final Function<String, ?> paramFun = RoutingContextEnvelope.paramFun(parameterType);
            return pathParamsFun.andThen(it -> it.getString(key)).andThen(paramFun);
        }

        final QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
        if (queryParam != null) {
            final String defaultValue = getDefaultValue(parameter);
            final String key = queryParam.value();
            final Function<JsonObject, JsonObject> queryParamsFun = RoutingContextEnvelope::queryParams;
            final Function<String, ?> paramFun = RoutingContextEnvelope.paramFun(parameterType);
            return queryParamsFun.andThen(queryParams -> Optional.ofNullable(queryParams.getJsonArray(key))
                    .filter(it -> !it.isEmpty())
                    .map(it -> it.getString(0))
                    .filter(J::nonBlank)
                    .orElse(defaultValue))
                    .andThen(paramFun);
        }

        final Function<JsonObject, String> bodyFun = RoutingContextEnvelope::body;
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

    private static String getDefaultValue(Parameter parameter) {
        return Optional.ofNullable(parameter.getAnnotation(DefaultValue.class))
                .map(DefaultValue::value)
                .orElse(null);
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
}
