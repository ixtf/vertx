package com.github.ixtf.vertx.apm;

import io.opentracing.propagation.TextMap;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;

import java.util.Iterator;
import java.util.Map;

/**
 * @author jzb 2019-06-25
 */
public class RCTextMapExtractAdapter implements TextMap {
    private final Iterator<Map.Entry<String, String>> iterator;

    public RCTextMapExtractAdapter(RoutingContext rc) {
        this.iterator = rc.request().headers().iterator();
    }

    public RCTextMapExtractAdapter(Message reply) {
        this.iterator = reply.headers().iterator();
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return iterator;
    }

    @Override
    public void put(String key, String value) {
        throw new UnsupportedOperationException("carrier is read-only");
    }
}
