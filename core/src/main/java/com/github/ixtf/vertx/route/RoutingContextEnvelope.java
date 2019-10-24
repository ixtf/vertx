package com.github.ixtf.vertx.route;

import com.github.ixtf.japp.core.J;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
