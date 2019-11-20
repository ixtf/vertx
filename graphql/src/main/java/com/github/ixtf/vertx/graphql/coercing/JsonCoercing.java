package com.github.ixtf.vertx.graphql.coercing;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.vertx.graphql.Jgraphql;
import graphql.language.*;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.util.Map;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author jzb 2019-10-02
 */
public class JsonCoercing implements Coercing<Object, Object> {

    @Override
    public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
        return dataFetcherResult instanceof JsonNode ? MAPPER.convertValue(dataFetcherResult, Map.class) : dataFetcherResult;
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        return input;
    }

    @Override
    public Object parseLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof ObjectValue) {
            final ObjectValue objectValue = (ObjectValue) input;
            return objectValue.getObjectFields().parallelStream()
                    .collect(toMap(ObjectField::getName, it -> {
                        final Value value = it.getValue();
                        return parseObjectFieldValue(value);
                    }));
        }
        throw new CoercingParseLiteralException("Expected AST type 'ObjectValue' but was '" + Jgraphql.typeName(input) + "'.");
    }

    private Object parseObjectFieldValue(Value value) {
        if (value instanceof NullValue) {
            return null;
        }
        if (value instanceof StringValue) {
            final StringValue stringValue = (StringValue) value;
            return stringValue.getValue();
        }
        if (value instanceof FloatValue) {
            final FloatValue floatValue = (FloatValue) value;
            return floatValue.getValue();
        }
        if (value instanceof IntValue) {
            final IntValue intValue = (IntValue) value;
            return intValue.getValue();
        }
        if (value instanceof BooleanValue) {
            final BooleanValue booleanValue = (BooleanValue) value;
            return booleanValue.isValue();
        }
        if (value instanceof ArrayValue) {
            final ArrayValue arrayValue = (ArrayValue) value;
            return arrayValue.getValues().stream()
                    .map(this::parseObjectFieldValue)
                    .collect(toList());
        }
        return parseLiteral(value);
    }
}
