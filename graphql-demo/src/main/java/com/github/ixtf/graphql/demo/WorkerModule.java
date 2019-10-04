package com.github.ixtf.graphql.demo;

import com.google.common.io.Resources;
import com.google.inject.*;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Vertx;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author jzb 2019-05-02
 */
public class WorkerModule extends AbstractModule {
    public static Injector INJECTOR;
    private final Vertx vertx;

    private WorkerModule(Vertx vertx) {
        this.vertx = vertx;
    }

    static void init(Vertx vertx) {
        INJECTOR = Guice.createInjector(new WorkerModule(vertx));
    }

    @Override
    protected void configure() {
        bind(Vertx.class).toInstance(vertx);
    }

    @Provides
    @Singleton
    private GraphQL GraphQL() {
        final SchemaParser schemaParser = new SchemaParser();
        final TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();
        Stream.of("mes-auto.graphql", "mes-auto-command.graphql", "mes-auto-type.graphql")
                .map(this::loadSdl)
                .map(schemaParser::parse)
                .forEach(typeDefinitionRegistry::merge);
        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final RuntimeWiring runtimeWiring = DataFetchers.buildWiring();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    @SneakyThrows
    private String loadSdl(String fileName) {
        final URL url = Resources.getResource(fileName);
        return Resources.toString(url, UTF_8);
    }

}
