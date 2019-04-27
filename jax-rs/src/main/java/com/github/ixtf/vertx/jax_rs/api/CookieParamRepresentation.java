package com.github.ixtf.vertx.jax_rs.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.Cookie;
import io.vertx.reactivex.ext.web.RoutingContext;

import javax.ws.rs.CookieParam;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
class CookieParamRepresentation extends MethodParamRepresentation {
    protected final Function<JsonNode, Object> argFun;
    private final CookieParam cookieParam;

    CookieParamRepresentation(MethodRepresentationJaxRs methodRepresentation, Parameter parameter) {
        super(methodRepresentation, parameter);
        cookieParam = parameter.getAnnotation(CookieParam.class);
        argFun = initArgFun();
    }

    @Override
    JsonNode getValue(RoutingContext rc) {
        final HttpServerRequest request = rc.request();
        final String value = rc.cookies().stream()
                .filter(cookie -> Objects.equals(cookie.getName(), cookieParam.value()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        return TextNode.valueOf(defaultValue(value));
    }

    Object getArg(JsonNode argNode) {
        return argFun.apply(argNode);
    }

}