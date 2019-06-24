package com.github.ixtf.vertx.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.Jvertx;
import com.sun.security.auth.UserPrincipal;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.apache.commons.lang3.math.NumberUtils;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toMap;

/**
 * @author jzb 2019-02-28
 */
class RCEnvelope {
    private final JsonObject jsonObject;

    RCEnvelope(Message<JsonObject> reply) {
        jsonObject = reply.body();
    }

    private static JsonObject SendMessage(RoutingContext rc) {
        final JsonObject principal = Optional.ofNullable(rc.user()).map(User::principal).orElse(null);
        final Map<String, String> pathParams = rc.pathParams();
        final Map<String, List<String>> queryParams = rc.queryParams().names().parallelStream().collect(toMap(Function.identity(), rc.queryParams()::getAll));
        return new JsonObject()
                .put("principal", principal)
                .put("pathParams", pathParams)
                .put("queryParams", queryParams)
                .put("body", rc.getBodyAsString());
    }

    static void send(RoutingContext rc, String address, DeliveryOptions deliveryOptions) {
        rc.vertx().eventBus().rxSend(address, SendMessage(rc), deliveryOptions).subscribe(reply -> {
            final HttpServerResponse response = rc.response();
            final MultiMap headers = reply.headers();
            headers.entries().forEach(it -> response.putHeader(it.getKey(), it.getValue()));
            final Object body = reply.body();
            if (body == null) {
                response.end();
            } else if (body instanceof String) {
                final String result = (String) body;
                if (J.isBlank(result)) {
                    response.end();
                } else {
                    response.end(result);
                }
            } else {
                final byte[] bytes = (byte[]) body;
                response.end(Buffer.buffer(bytes));
            }
        }, rc::fail);
    }

    static Function<RCEnvelope, ? extends Object> argFun(Parameter parameter) {
        final Class<?> parameterType = parameter.getType();
        if (Principal.class.isAssignableFrom(parameterType)) {
            return envelope -> {
                final JsonObject jsonObject = envelope.principal();
                final String uid = jsonObject.getString("uid");
                return new UserPrincipal(uid);
            };
        }

        final PathParam pathParam = parameter.getAnnotation(PathParam.class);
        if (pathParam != null) {
            final String key = pathParam.value();
            return envelope -> {
                final String s = envelope.pathParam(key);
                return primitiveFun(parameterType).apply(s);
            };
        }

        final String defaultValue = Optional.ofNullable(parameter.getAnnotation(DefaultValue.class)).map(DefaultValue::value).orElse(null);
        final QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
        if (queryParam != null) {
            final String key = queryParam.value();
            return envelope -> {
                final String s = envelope.queryParam(key, defaultValue);
                return primitiveFun(parameterType).apply(s);
            };
        }

        if (String.class == parameterType) {
            return envelope -> envelope.body();
        }
        if (JsonObject.class == parameterType) {
            return envelope -> new JsonObject(envelope.body());
        }
        if (JsonArray.class == parameterType) {
            return envelope -> new JsonArray(envelope.body());
        }
        if (JsonNode.class == parameterType) {
            return envelope -> {
                try {
                    return MAPPER.readValue(envelope.body(), JsonNode.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };
        }
        return envelope -> Jvertx.checkAndGetCommand(parameterType, envelope.body());
    }

    private static Function<String, ? extends Object> primitiveFun(Class<?> parameterType) {
        if (String.class.isAssignableFrom(parameterType)) {
            return it -> it;
        }

        if (boolean.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? (boolean) false : Boolean.valueOf(it).booleanValue();
        }
        if (Boolean.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Boolean.valueOf(it);
        }

        if (int.class.isAssignableFrom(parameterType)) {
            return it -> NumberUtils.toInt(it);
        }
        if (Integer.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Integer.valueOf(it);
        }

        if (float.class.isAssignableFrom(parameterType)) {
            return it -> NumberUtils.toFloat(it);
        }
        if (Float.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Float.valueOf(it);
        }

        if (double.class.isAssignableFrom(parameterType)) {
            return it -> NumberUtils.toDouble(it);
        }
        if (Double.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Double.valueOf(it);
        }

        if (short.class.isAssignableFrom(parameterType)) {
            return it -> NumberUtils.toShort(it);
        }
        if (Short.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Short.valueOf(it);
        }

        if (byte.class.isAssignableFrom(parameterType)) {
            return it -> NumberUtils.toByte(it);
        }
        if (Byte.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Byte.valueOf(it);
        }

        throw new UnsupportedOperationException();
    }

    private JsonObject principal() {
        return jsonObject.getJsonObject("principal");
    }

    private String body() {
        return jsonObject.getString("body", null);
    }

    private JsonObject pathParams() {
        return jsonObject.getJsonObject("pathParams");
    }

    private String pathParam(String key) {
        return pathParams().getString(key);
    }

    private JsonObject queryParams() {
        return jsonObject.getJsonObject("queryParams");
    }

    private String queryParam(String key, String defaultValue) {
        final JsonArray jsonArray = queryParams().getJsonArray(key);
        if (jsonArray == null || jsonArray.isEmpty()) {
            return defaultValue;
        }
        final String result = jsonArray.getString(0);
        return result == null ? defaultValue : result;
    }
}
