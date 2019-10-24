package com.github.ixtf.vertx.graphql.config;

import com.github.ixtf.vertx.JvertxOptions;
import com.github.ixtf.vertx.VerticleRepresentation;
import com.github.ixtf.vertx.graphql.GraphQLMutation;
import com.github.ixtf.vertx.graphql.GraphQLQuery;
import com.google.inject.Injector;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.graphql.impl.GraphQLInput;
import lombok.Getter;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author jzb 2019-02-16
 */
public class GraphQLVerticleRepresentation implements VerticleRepresentation {
    private final Injector injector;
    @Getter
    private final Class<?> resourceClass;
    @Getter
    private final Method method;
    @Getter
    private final GraphQLQuery query;
    @Getter
    private final GraphQLMutation mutation;
    @Getter
    private final String fName;
    @Getter
    private final String address;

    GraphQLVerticleRepresentation(Injector injector, Class<?> resourceClass, Method method) {
        this.injector = injector;
        this.resourceClass = resourceClass;
        this.method = method;
        query = method.getAnnotation(GraphQLQuery.class);
        mutation = method.getAnnotation(GraphQLMutation.class);
        if (query != null) {
            fName = query.value();
            address = "GraphQL:Query" + fName;
        } else if (mutation != null) {
            fName = mutation.value();
            address = "GraphQL:Mutation" + fName;
        } else {
            throw new RuntimeException("address not valid");
        }
    }

    @Override
    public Future<String> deploy(Vertx vertx) {
        return Future.future(promise -> vertx.deployVerticle(V.class, deploymentOptions(), promise));
    }

    public DeploymentOptions deploymentOptions() {
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
        final JvertxOptions jvertxOptions = method.getAnnotation(JvertxOptions.class);
        if (jvertxOptions != null) {
            deploymentOptions.setInstances(jvertxOptions.instances());
        }
        return deploymentOptions;
    }

    public class V extends AbstractVerticle {
        @Override
        public void start(Future<Void> startFuture) throws Exception {
            final Class<?> returnType = method.getReturnType();
            if (Mono.class.isAssignableFrom(returnType)) {

            }
            final GraphQL graphQL = injector.getInstance(GraphQL.class);
            vertx.eventBus().<String>consumer(address, reply -> {
                final io.vertx.ext.web.handler.graphql.impl.GraphQLQuery query = (io.vertx.ext.web.handler.graphql.impl.GraphQLQuery) Json.decodeValue(reply.body(), GraphQLInput.class);
                final ExecutionInput.Builder builder = ExecutionInput.newExecutionInput().context(reply).query(query.getQuery());
                final String operationName = query.getOperationName();
                if (operationName != null) {
                    builder.operationName(operationName);
                }
                final Map<String, Object> variables = query.getVariables();
                if (variables != null) {
                    builder.variables(variables);
                }
                final ExecutionResult executionResult = graphQL.execute(builder.build());
                final Map data = executionResult.getData();
                final JsonObject jsonObject = new JsonObject(data);
                reply.reply(jsonObject.getValue(fName));
            }).completionHandler(startFuture);
        }
    }
}
