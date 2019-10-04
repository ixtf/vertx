package com.github.ixtf.graphql.demo;

import com.github.ixtf.vertx.graphql.Jgraphql;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.WiringFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.ext.web.handler.graphql.VertxPropertyDataFetcher;
import io.vertx.ext.web.handler.graphql.impl.GraphQLBatch;
import io.vertx.ext.web.handler.graphql.impl.GraphQLInput;
import io.vertx.ext.web.handler.graphql.impl.GraphQLQuery;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-09-24
 */
public class DataFetchers {

    public static RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .scalar(Jgraphql.getGraphQLLocalDate())
                .scalar(Jgraphql.getGraphQLLocalDateTime())
                .wiringFactory(new WiringFactory() {
                    @Override
                    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
                        return new VertxPropertyDataFetcher(environment.getFieldDefinition().getName());
                    }
                })
                .type("Query", builder -> builder
                        .dataFetcher("hello", env -> {
                            env.getRoot();
                            return Thread.currentThread().toString();
                        })
                        .dataFetcher("listOperator", listOperator())
                ).build();
    }

    private static DataFetcher listOperator() {
        return new VertxDataFetcher<>((env, promise) -> {
            final RoutingContext rc = env.getContext();
            final GraphQLInput graphQLInput = Json.decodeValue(rc.getBody(), GraphQLInput.class);
            if (graphQLInput instanceof GraphQLQuery) {
                handleQuery(env, promise, rc, (GraphQLQuery) graphQLInput);
            } else if (graphQLInput instanceof GraphQLBatch) {
                handleBatch(env, promise, rc, (GraphQLBatch) graphQLInput);
            } else {
                promise.fail("500");
            }
        });
    }

    private static void handleQuery(DataFetchingEnvironment env, Promise<Object> promise, RoutingContext rc, GraphQLQuery graphQLQuery) {
        final Mono<String> address$ = Mono.fromCallable(() -> "graphql:Query:" + env.getField().getName()).subscribeOn(Schedulers.elastic());
        final Mono<String> message$ = Mono.fromCallable(() -> Json.encode(graphQLQuery)).subscribeOn(Schedulers.elastic());
        final Mono<DeliveryOptions> deliveryOptions$ = Mono.fromCallable(() -> new DeliveryOptions()).subscribeOn(Schedulers.elastic());
        Mono.zip(address$, message$, deliveryOptions$).map(tuple3 ->
                Future.<Message<Object>>future(p -> rc.vertx().eventBus().request(tuple3.getT1(), tuple3.getT2(), tuple3.getT3(), p))
                        .map(Message::body).setHandler(promise)
        ).subscribe();
    }

    private static void handleBatch(DataFetchingEnvironment env, Promise<Object> promise, RoutingContext rc, GraphQLBatch graphQLBatch) {
        final List<String> batchQuery = graphQLBatch.stream().map(GraphQLQuery::getQuery).collect(toList());
    }
}
