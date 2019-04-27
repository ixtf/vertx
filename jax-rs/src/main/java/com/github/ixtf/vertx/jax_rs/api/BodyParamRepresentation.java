package com.github.ixtf.vertx.jax_rs.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.ixtf.vertx.Jvertx;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.SneakyThrows;

import java.lang.reflect.Parameter;

/**
 * @author jzb 2019-02-14
 */
class BodyParamRepresentation extends MethodParamRepresentation {

    BodyParamRepresentation(MethodRepresentationJaxRs methodRepresentation, Parameter parameter) {
        super(methodRepresentation, parameter);
    }

    @Override
    JsonNode getValue(RoutingContext rc) {
        return TextNode.valueOf(rc.getBodyAsString());
    }

    @SneakyThrows
    @Override
    Object getArg(JsonNode argNode) {
        return Jvertx.readCommand(parameterClass, argNode.asText());
    }

}