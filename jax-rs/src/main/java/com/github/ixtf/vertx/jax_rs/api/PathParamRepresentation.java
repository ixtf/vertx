package com.github.ixtf.vertx.jax_rs.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.vertx.reactivex.ext.web.RoutingContext;

import javax.ws.rs.PathParam;
import java.lang.reflect.Parameter;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
class PathParamRepresentation extends MethodParamRepresentation {
    protected final Function<JsonNode, Object> argFun;
    private final PathParam pathParam;

    PathParamRepresentation(MethodRepresentationJaxRs methodRepresentation, Parameter parameter) {
        super(methodRepresentation, parameter);
        pathParam = parameter.getAnnotation(PathParam.class);
        argFun = initArgFun();
    }

    @Override
    JsonNode getValue(RoutingContext rc) {
        final String value = rc.pathParam(pathParam.value());
        return TextNode.valueOf(defaultValue(value));
    }

    Object getArg(JsonNode argNode) {
        return argFun.apply(argNode);
    }

}