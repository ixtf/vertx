package com.github.ixtf.vertx.jax_rs.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.vertx.reactivex.ext.web.RoutingContext;

import javax.ws.rs.MatrixParam;
import java.lang.reflect.Parameter;

/**
 * todo MatrixParam支持
 *
 * @author jzb 2019-02-14
 */
class MatrixParamRepresentation extends MethodParamRepresentation {
    private final MatrixParam matrixParam;

    MatrixParamRepresentation(MethodRepresentationJaxRs methodRepresentation, Parameter parameter) {
        super(methodRepresentation, parameter);
        matrixParam = parameter.getAnnotation(MatrixParam.class);
    }

    @Override
    JsonNode getValue(RoutingContext rc) {
        throw new UnsupportedOperationException();
    }

    @Override
    Object getArg(JsonNode argNode) {
        throw new UnsupportedOperationException();
    }
}