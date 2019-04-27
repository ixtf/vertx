package com.github.ixtf.vertx;

import com.github.ixtf.japp.core.J;
import com.sun.security.auth.UserPrincipal;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.web.RoutingContext;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.Serializable;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * @author jzb 2019-02-28
 */
public class RCEnvelope implements Serializable {
    private final JsonObject jsonObject;

    RCEnvelope(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
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
            final Object replyBody = reply.body();
            if (replyBody == null) {
                response.end();
                return;
            }
            if (replyBody instanceof String) {
                final String result = (String) replyBody;
                if (J.isBlank(result)) {
                    response.end();
                } else {
                    response.end(result);
                }
                return;
            }
            final byte[] bytes = (byte[]) replyBody;
            response.end(Buffer.buffer(bytes));
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
        return envelope -> Jvertx.checkAndGetCommand(parameterType, envelope.body());
    }

    private static Function<String, ? extends Object> primitiveFun(Class<?> parameterType) {
        if (String.class.isAssignableFrom(parameterType)) {
            return it -> it;
        }

        if (int.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? (int) 0 : Integer.valueOf(it).intValue();
        }
        if (Integer.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Integer.valueOf(it);
        }

        if (float.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? (float) 0 : Float.valueOf(it).floatValue();
        }
        if (Float.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Float.valueOf(it);
        }

        if (double.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? (double) 0 : Double.valueOf(it).doubleValue();
        }
        if (Double.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Double.valueOf(it);
        }

        if (short.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? (short) 0 : Short.valueOf(it).shortValue();
        }
        if (Short.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? null : Short.valueOf(it);
        }

        if (byte.class.isAssignableFrom(parameterType)) {
            return it -> J.isBlank(it) ? (byte) 0 : Byte.valueOf(it).byteValue();
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
