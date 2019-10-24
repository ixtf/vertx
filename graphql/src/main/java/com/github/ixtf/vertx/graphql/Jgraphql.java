package com.github.ixtf.vertx.graphql;

import com.github.ixtf.vertx.graphql.coercing.LocalDateCoercing;
import com.github.ixtf.vertx.graphql.coercing.LocalDateTimeCoercing;
import graphql.schema.GraphQLScalarType;
import lombok.Getter;

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

    public static String typeName(Object input) {
        if (input == null) {
            return "null";
        }
        return input.getClass().getSimpleName();
    }

}
