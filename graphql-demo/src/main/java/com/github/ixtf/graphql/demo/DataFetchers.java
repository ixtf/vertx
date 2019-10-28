package com.github.ixtf.graphql.demo;

import com.github.ixtf.vertx.graphql.Jgraphql;
import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.WiringFactory;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.ext.web.handler.graphql.VertxPropertyDataFetcher;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                )
                .type("Mutation", builder -> builder
                        .dataFetcher("createOperator", createOperator())
                ).build();
    }

    private static DataFetcher listOperator() {
        return new VertxDataFetcher<>((env, promise) -> {
            final long first = env.getArgument("first");
            final int pageSize = env.getArgument("pageSize");
            final long count = 12345;
            final List<Map> operators = IntStream.range(0, 10).mapToObj(i ->
                    Map.of("id", i, "name", "name" + i, "birthDate", new Date(), "cdt", new Date())
            ).collect(Collectors.toList());
            final Map<String, Object> map = Map.of("first", first, "pageSize", pageSize, "count", count, "operators", operators);
            promise.complete(map);
        });
    }

    private static DataFetcher createOperator() {
        return new VertxDataFetcher<>((env, promise) -> {
            final Map<String, Object> command = env.getArgument("command");
            System.out.println(command);
            final Map<String, Object> map = Map.of("id", "id", "name", command.get("name"), "birthDate", new Date(), "cdt", new Date());
            promise.complete(map);
        });
    }
}
