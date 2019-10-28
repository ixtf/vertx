package com.github.ixtf.graphql.demo;

import com.github.ixtf.japp.core.J;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.graphql.impl.GraphQLInput;
import io.vertx.ext.web.handler.graphql.impl.GraphQLQuery;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.github.ixtf.graphql.demo.WorkerModule.INJECTOR;

/**
 * @author jzb 2019-09-23
 */
@Slf4j
public class GraphqlWorker extends AbstractVerticle {

    public static void main(String[] args) {
        Future.<Vertx>future(promise -> {
            final VertxOptions vertxOptions = new VertxOptions()
                    .setWorkerPoolSize(1)
                    .setMaxWorkerExecuteTime(1)
                    .setMaxWorkerExecuteTimeUnit(TimeUnit.DAYS);
            Optional.ofNullable(System.getProperty("vertx.cluster.host")).filter(J::nonBlank)
                    .ifPresent(vertxOptions.getEventBusOptions()::setHost);
            Vertx.clusteredVertx(vertxOptions, promise);
        }).compose(vertx -> {
            WorkerModule.init(vertx);
            return Future.<String>future(p -> {
                final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
                vertx.deployVerticle(GraphqlWorker.class, deploymentOptions, p);
            });
        }).setHandler(ar -> {
            if (ar.succeeded()) {
                System.out.println("worker success");
            } else {
                ar.cause().printStackTrace();
            }
        });
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final GraphQL graphQL = INJECTOR.getInstance(GraphQL.class);
        CompositeFuture.all(
                consumerQuery(graphQL, "listOperator"),
                consumerMutation(graphQL, "createOperator")
        ).<Void>mapEmpty().setHandler(startFuture);
    }

    private Future<Void> consumerQuery(GraphQL graphQL, final String fName) {
        return Future.future(p -> vertx.eventBus().<String>consumer("graphql:Query:" + fName, reply -> {
            final GraphQLQuery query = (GraphQLQuery) Json.decodeValue(reply.body(), GraphQLInput.class);
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
        }).completionHandler(p));
    }

    private Future<Void> consumerMutation(GraphQL graphQL, final String fName) {
        return Future.future(p -> vertx.eventBus().<String>consumer("graphql:Mutation:" + fName, reply -> {
            final GraphQLQuery query = (GraphQLQuery) Json.decodeValue(reply.body(), GraphQLInput.class);
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
        }).completionHandler(p));
    }
}
