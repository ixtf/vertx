package com.github.ixtf.vertx.util;

import io.vertx.core.eventbus.DeliveryOptions;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-28
 */
public class Envelope {
    @Getter
    private Object data;
    @Getter
    private DeliveryOptions deliveryOptions = new DeliveryOptions();

    private Envelope() {
    }

    @SneakyThrows
    public static Envelope data(Object data) {
        if (data instanceof Envelope) {
            return (Envelope) data;
        }
        if (data instanceof Optional) {
            final Optional optional = (Optional) data;
            return Envelope.data(optional.orElse(null));
        }
        final Envelope envelope = new Envelope();
        if (data == null) {
            envelope.data = null;
        } else if (data instanceof String || data instanceof byte[]) {
            envelope.data = data;
        } else {
            envelope.data = MAPPER.writeValueAsString(data);
        }
        return envelope;
    }

    public Envelope putHeader(String name, String value) {
        deliveryOptions.addHeader(name, value);
        return this;
    }
}
