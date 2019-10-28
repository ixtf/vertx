package com.github.ixtf.vertx.graphql.coercing;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.graphql.Jgraphql;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;

/**
 * @author jzb 2019-10-02
 */
public class LocalDateCoercing implements Coercing<Object, Object> {
    public static LocalDate convertImpl(Object o) {
        if (o instanceof LocalDate) {
            return (LocalDate) o;
        }
        if (o instanceof Date) {
            return J.localDate((Date) o);
        }
        if (o instanceof LocalDateTime) {
            return ((LocalDateTime) o).toLocalDate();
        }
        if (o instanceof Number) {
            final long l = ((Number) o).longValue();
            return J.localDate(new Date(l));
        }
        if (o instanceof String) {
            final String s = (String) o;
            try {
                return LocalDate.parse(s);
            } catch (DateTimeParseException e) {
                if (NumberUtils.isParsable(s)) {
                    final long l = NumberUtils.toLong(s);
                    return J.localDate(new Date(l));
                }
            }
        }
        return null;
    }

    @Override
    public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
        return Optional.ofNullable(convertImpl(dataFetcherResult))
                .map(J::date)
                .map(Date::getTime)
                .orElseThrow(() -> {
                    final String typeName = Jgraphql.typeName(dataFetcherResult);
                    return new CoercingSerializeException("Expected type 'LocalDate' but was '" + typeName + "'.");
                });
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        return Optional.ofNullable(convertImpl(input))
                .orElseThrow(() -> {
                    final String typeName = Jgraphql.typeName(input);
                    return new CoercingSerializeException("Expected type 'LocalDate' but was '" + typeName + "'.");
                });
    }

    @Override
    public Object parseLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof StringValue) {
            return Optional.ofNullable(convertImpl(((StringValue) input).getValue()))
                    .orElseThrow(() -> new CoercingParseLiteralException("Unable to turn AST input into a 'LocalDateTime' : '" + input + "'"));
        } else if (input instanceof IntValue) {
            final long l = ((IntValue) input).getValue().longValue();
            return J.localDate(new Date(l));
        } else if (input instanceof FloatValue) {
            final long l = ((FloatValue) input).getValue().longValue();
            return J.localDate(new Date(l));
        }
        throw new CoercingParseLiteralException("Expected AST type 'IntValue', 'IntValue' or 'FloatValue' but was '" + Jgraphql.typeName(input) + "'.");
    }
}
