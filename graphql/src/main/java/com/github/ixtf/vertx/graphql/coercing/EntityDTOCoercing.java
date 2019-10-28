package com.github.ixtf.vertx.graphql.coercing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.vertx.graphql.Jgraphql;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jzb 2019-10-02
 */
public class EntityDTOCoercing implements Coercing<Object, Object> {
    public static Map convertImpl(Object o) {
        if (o instanceof Map) {
            return (Map) o;
        }
        if (o instanceof JsonObject) {
            final JsonObject jsonObject = (JsonObject) o;
            return jsonObject.getMap();
        }
        if (o instanceof ObjectNode) {
            final JsonNode jsonNode = (JsonNode) o;
            final JsonObject jsonObject = new JsonObject(jsonNode.toString());
            return jsonObject.getMap();
        }
        return null;
    }

    @Override
    public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
        return convertImpl(dataFetcherResult);
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        return convertImpl(input);
    }

    @Override
    public Object parseLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof ObjectValue) {
            final ObjectValue objectValue = (ObjectValue) input;
            return objectValue.getObjectFields().stream()
                    .filter(it -> "id".equals(it.getName()))
                    .collect(Collectors.toMap(ObjectField::getName, it -> {
                        final StringValue stringValue = (StringValue) it.getValue();
                        return stringValue.getValue();
                    }));
        }
        throw new CoercingParseLiteralException("Expected AST type 'ObjectValue' but was '" + Jgraphql.typeName(input) + "'.");
    }
}
