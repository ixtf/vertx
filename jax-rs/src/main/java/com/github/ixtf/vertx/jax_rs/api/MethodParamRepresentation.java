package com.github.ixtf.vertx.jax_rs.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.japp.core.J;
import io.vertx.reactivex.ext.web.RoutingContext;

import javax.ws.rs.DefaultValue;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
abstract class MethodParamRepresentation {
    protected final MethodRepresentationJaxRs methodRepresentation;
    protected final Parameter parameter;
    protected final Class parameterClass;
    protected final DefaultValue defaultValue;

    MethodParamRepresentation(MethodRepresentationJaxRs methodRepresentation, Parameter parameter) {
        this.methodRepresentation = methodRepresentation;
        this.parameter = parameter;
        parameterClass = parameter.getType();
        defaultValue = parameter.getAnnotation(DefaultValue.class);
    }

    MethodParamRepresentation(MethodRepresentationJaxRs methodRepresentation, Parameter parameter, Function<JsonNode, Object> argFun) {
        this.methodRepresentation = methodRepresentation;
        this.parameter = parameter;
        parameterClass = parameter.getType();
        defaultValue = parameter.getAnnotation(DefaultValue.class);
    }

    private Function<JsonNode, Object> filterNull(Function<JsonNode, Object> fun) {
        return jsonNode -> Optional.ofNullable(jsonNode).filter(it -> !it.isNull()).map(fun).orElse(null);
    }

    protected Function<JsonNode, Object> initArgFun() {
        if (String.class.isAssignableFrom(parameterClass)) {
            return filterNull(JsonNode::asText);
        }
        if (Boolean.class.isAssignableFrom(parameterClass)) {
            return filterNull(JsonNode::asBoolean);
        }
        if (boolean.class.isAssignableFrom(parameterClass)) {
            return JsonNode::asBoolean;
        }
        if (Integer.class.isAssignableFrom(parameterClass)) {
            return filterNull(JsonNode::asInt);
        }
        if (int.class.isAssignableFrom(parameterClass)) {
            return JsonNode::asInt;
        }
        if (Long.class.isAssignableFrom(parameterClass)) {
            return filterNull(JsonNode::asLong);
        }
        if (long.class.isAssignableFrom(parameterClass)) {
            return JsonNode::asLong;
        }
        if (Double.class.isAssignableFrom(parameterClass)) {
            return filterNull(JsonNode::asDouble);
        }
        if (double.class.isAssignableFrom(parameterClass)) {
            return JsonNode::asDouble;
        }
        if (LocalDate.class.isAssignableFrom(parameterClass)) {
            return filterNull(jsonNode -> {
                if (jsonNode.isNumber()) {
                    final Date date = new Date(jsonNode.longValue());
                    return J.localDate(date);
                }
                return LocalDate.parse(jsonNode.asText());
            });
        }
        if (Date.class.isAssignableFrom(parameterClass)) {
            return filterNull(jsonNode -> {
                if (jsonNode.isNumber()) {
                    return new Date(jsonNode.longValue());
                }
                return new Date(jsonNode.asText());
            });
        }
        final Function<JsonNode, Integer> asInt = JsonNode::asInt;
        if (Byte.class.isAssignableFrom(parameterClass)) {
            return filterNull(asInt.andThen(Integer::byteValue));
        }
        if (byte.class.isAssignableFrom(parameterClass)) {
            return asInt.andThen(Integer::byteValue);
        }
        if (Short.class.isAssignableFrom(parameterClass)) {
            return filterNull(asInt.andThen(Integer::shortValue));
        }
        if (short.class.isAssignableFrom(parameterClass)) {
            return asInt.andThen(Integer::shortValue);
        }
        final Function<JsonNode, Double> asDouble = JsonNode::asDouble;
        if (Float.class.isAssignableFrom(parameterClass)) {
            return filterNull(asDouble.andThen(Double::floatValue));
        }
        if (float.class.isAssignableFrom(parameterClass)) {
            return asDouble.andThen(Double::floatValue);
        }
        if (BigDecimal.class.isAssignableFrom(parameterClass)) {
            return filterNull(asDouble.andThen(BigDecimal::valueOf));
        }
        throw new UnsupportedOperationException("类型：" + parameterClass + "不支持！");
    }

    protected String defaultValue(String value) {
        if (value == null && defaultValue != null) {
            return defaultValue.value();
        }
        return value;
    }

    abstract JsonNode getValue(RoutingContext rc);

    abstract Object getArg(JsonNode argNode);
}