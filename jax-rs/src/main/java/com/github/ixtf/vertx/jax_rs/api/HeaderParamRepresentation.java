package com.github.ixtf.vertx.jax_rs.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.RoutingContext;

import javax.ws.rs.HeaderParam;
import java.lang.reflect.Parameter;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
class HeaderParamRepresentation extends MethodParamRepresentation {
    protected final Function<JsonNode, Object> argFun;
    private final HeaderParam headerParam;

    HeaderParamRepresentation(MethodRepresentationJaxRs methodRepresentation, Parameter parameter) {
        super(methodRepresentation, parameter);
        headerParam = parameter.getAnnotation(HeaderParam.class);
        argFun = initArgFun();
    }

    @Override
    JsonNode getValue(RoutingContext rc) {
        final HttpServerRequest request = rc.request();
        final String value = request.getHeader(headerParam.value());
        return TextNode.valueOf(defaultValue(value));
    }

    Object getArg(JsonNode argNode) {
        return argFun.apply(argNode);
    }

}