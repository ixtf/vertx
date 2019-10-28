package com.github.ixtf.jax.rs.demo.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.ixtf.jax.rs.demo.domain.Product;

import java.io.IOException;

/**
 * @author jzb 2018-06-28
 */
public class ProductEmbedSerializer extends JsonSerializer<Product> {
    @Override
    public void serialize(Product value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            return;
        }

        gen.writeStartObject();
        gen.writeStringField("id", value.getId());
        gen.writeStringField("name", value.getName());
        gen.writeEndObject();
    }
}
