package com.github.ixtf.vertx.graphql;

import com.github.ixtf.vertx.graphql.coercing.EntityDTOCoercing;
import com.github.ixtf.vertx.graphql.coercing.JsonCoercing;
import com.github.ixtf.vertx.graphql.coercing.LocalDateCoercing;
import com.github.ixtf.vertx.graphql.coercing.LocalDateTimeCoercing;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author jzb 2019-10-02
 */
public class Jgraphql {
    @Getter(lazy = true)
    private static final GraphQLScalarType GraphQLLocalDate = GraphQLScalarType.newScalar()
            .name("LocalDate")
            .description("Jgraphql LocalDate")
            .coercing(new LocalDateCoercing())
            .build();
    @Getter(lazy = true)
    private static final GraphQLScalarType GraphQLLocalDateTime = GraphQLScalarType.newScalar()
            .name("LocalDateTime")
            .description("Jgraphql LocalDateTime")
            .coercing(new LocalDateTimeCoercing())
            .build();
    @Getter(lazy = true)
    private static final GraphQLScalarType GraphQLJson = GraphQLScalarType.newScalar()
            .name("Json")
            .description("Jgraphql Json")
            .coercing(new JsonCoercing())
            .build();
    @Getter(lazy = true)
    private static final GraphQLScalarType GraphQLMap = GraphQLScalarType.newScalar()
            .name("EntityDTO")
            .description("Jgraphql EntityDTO")
            .coercing(new EntityDTOCoercing())
            .build();

    public static String typeName(Object input) {
        if (input == null) {
            return "null";
        }
        return input.getClass().getSimpleName();
    }

    public static GraphQL newGraphQL(RuntimeWiring runtimeWiring, String... urls) {
        final SchemaParser schemaParser = new SchemaParser();
        final TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();
        Stream.of(urls).map(Jgraphql::loadSdl).map(schemaParser::parse).forEach(typeDefinitionRegistry::merge);
        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    @SneakyThrows
    public static String loadSdl(String fileName) {
        final URL url = Resources.getResource(fileName);
        return Resources.toString(url, UTF_8);
    }

}
