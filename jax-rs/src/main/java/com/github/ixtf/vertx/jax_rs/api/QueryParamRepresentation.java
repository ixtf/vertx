package com.github.ixtf.vertx.jax_rs.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.ixtf.japp.core.J;
import io.vertx.reactivex.ext.web.RoutingContext;

import javax.ws.rs.QueryParam;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.function.Function;

/**
 * @author jzb 2019-02-14
 */
class QueryParamRepresentation extends MethodParamRepresentation {
    protected final Function<JsonNode, Object> argFun;
    private final QueryParam queryParam;

    QueryParamRepresentation(MethodRepresentationJaxRs methodRepresentation, Parameter parameter) {
        super(methodRepresentation, parameter);
        queryParam = parameter.getAnnotation(QueryParam.class);
        argFun = initArgFun();
    }

    @Override
    JsonNode getValue(RoutingContext rc) {
        final List<String> values = rc.queryParam(queryParam.value());
        final String value = J.isEmpty(values) ? null : values.get(0);
        return TextNode.valueOf(defaultValue(value));
    }

    Object getArg(JsonNode argNode) {
        return argFun.apply(argNode);
    }

}