package com.github.ixtf.vertx.jax_rs.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.ixtf.vertx.api.VertxDelivery;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-14
 */
class MethodRepresentationJaxRs {
    @Getter
    private final RouteRepresentationJaxRs routeRepresentation;
    @Getter
    private final Method nativeMethod;
    @Getter
    private final Class<?> returnClass;
    private final int parameterCount;
    @Getter
    private final Function<RoutingContext, DeliveryOptions> deliveryOptionsFun;
    private final MethodParamRepresentation[] paramRepresentations;

    protected MethodRepresentationJaxRs(RouteRepresentationJaxRs routeRepresentation, Method nativeMethod) {
        this.routeRepresentation = routeRepresentation;
        this.nativeMethod = nativeMethod;
        returnClass = nativeMethod.getReturnType();
        parameterCount = nativeMethod.getParameterCount();
        this.deliveryOptionsFun = initDeliveryOptionsFun();
        this.paramRepresentations = initParamRepresentations();
    }

    private Function<RoutingContext, DeliveryOptions> initDeliveryOptionsFun() {
        final Function<RoutingContext, DeliveryOptions> defaultFun = rc -> {
            final DeliveryOptions deliveryOptions = new DeliveryOptions();
            return deliveryOptions;
        };
        return Optional.ofNullable(nativeMethod.getAnnotation(VertxDelivery.class))
                .map(annotation -> defaultFun.andThen(deliveryOptions -> {
                    deliveryOptions.setSendTimeout(annotation.timeout());
                    return deliveryOptions;
                }))
                .orElse(defaultFun);
    }

    private MethodParamRepresentation[] initParamRepresentations() {
        if (parameterCount == 0) {
            return new MethodParamRepresentation[0];
        }
        return Arrays.stream(nativeMethod.getParameters()).map(parameter -> {
            final Class<?> parameterType = parameter.getType();
            if (Principal.class.isAssignableFrom(parameterType)) {
                return new PrincipalParamRepresentation(this, parameter);
            }
            final PathParam pathParam = parameter.getAnnotation(PathParam.class);
            if (pathParam != null) {
                return new PathParamRepresentation(this, parameter);
            }
            final QueryParam queryParam = parameter.getAnnotation(QueryParam.class);
            if (queryParam != null) {
                return new QueryParamRepresentation(this, parameter);
            }
            final HeaderParam headerParam = parameter.getAnnotation(HeaderParam.class);
            if (headerParam != null) {
                return new HeaderParamRepresentation(this, parameter);
            }
            final CookieParam cookieParam = parameter.getAnnotation(CookieParam.class);
            if (cookieParam != null) {
                return new CookieParamRepresentation(this, parameter);
            }
            return new BodyParamRepresentation(this, parameter);
        }).toArray(MethodParamRepresentation[]::new);
    }

    Function<RoutingContext, String> getMessageFun() {
        return rc -> {
            final ArrayNode arrayNode = MAPPER.createArrayNode();
            Arrays.stream(paramRepresentations)
                    .map(it -> it.getValue(rc))
                    .forEach(arrayNode::add);
            return arrayNode.toString();
        };
    }

    Function<String, Object[]> getArgsFun() {
        return this::getArgs;
    }

    @SneakyThrows
    private Object[] getArgs(String body) {
        final JsonNode argsNode = MAPPER.readTree(body);
        final int argsSize = argsNode.size();
        final Object[] result = new Object[argsSize];
        for (int i = 0; i < argsSize; i++) {
            final JsonNode argNode = argsNode.get(i);
            result[i] = paramRepresentations[i].getArg(argNode);
        }
        return result;
    }

}