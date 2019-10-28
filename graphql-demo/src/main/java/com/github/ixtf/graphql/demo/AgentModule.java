package com.github.ixtf.graphql.demo;

import com.github.ixtf.vertx.graphql.JgraphqlModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Vertx;

import java.util.stream.Stream;

/**
 * @author jzb 2019-05-02
 */
public class AgentModule extends JgraphqlModule {
    public static Injector INJECTOR;
    private final Vertx vertx;

    private AgentModule(Vertx vertx) {
        this.vertx = vertx;
    }

    static void init(Vertx vertx) {
        INJECTOR = Guice.createInjector(new AgentModule(vertx));
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
        final RuntimeWiring runtimeWiring = buildWiring();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

}
