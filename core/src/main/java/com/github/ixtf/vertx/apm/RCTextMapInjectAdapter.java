package com.github.ixtf.vertx.apm;

import io.opentracing.propagation.TextMap;
import io.vertx.core.eventbus.DeliveryOptions;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author jzb 2019-06-25
 */
public class RCTextMapInjectAdapter implements TextMap {
    private final BiConsumer<String, String> consumer;

    public RCTextMapInjectAdapter(DeliveryOptions deliveryOptions) {
        consumer = (key, value) -> deliveryOptions.addHeader(key, value);
    }

    @Override
    public void put(String key, String value) {
        consumer.accept(key, value);
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("carrier is write-only");
    }
}
