package com.github.ixtf.vertx.jax_rs.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.spi.ResourceProvider;
import com.sun.security.auth.UserPrincipal;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.lang.reflect.Parameter;
import java.util.Optional;

/**
 * @author jzb 2019-02-14
 */
class PrincipalParamRepresentation extends MethodParamRepresentation {
    PrincipalParamRepresentation(MethodRepresentationJaxRs methodRepresentation, Parameter parameter) {
        super(methodRepresentation, parameter);
    }

    @Override
    JsonNode getValue(RoutingContext rc) {
        final ResourceProvider resourceProvider = methodRepresentation.getRouteRepresentation().getResourceRepresentation().getResourceProvider();
        return resourceProvider.principal(rc)
                .map(TextNode::valueOf)
                .map(it -> (JsonNode) it)
                .orElse(NullNode.getInstance());
    }

    @Override
    Object getArg(JsonNode argNode) {
        return Optional.ofNullable(argNode)
                .filter(it -> !it.isNull())
                .map(JsonNode::asText)
                .filter(J::nonBlank)
                .map(UserPrincipal::new)
                .orElse(null);
    }
}