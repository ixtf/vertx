package com.github.ixtf.vertx;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.SneakyThrows;

import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-28
 */
public class Envelope {
    private Object data;
    private DeliveryOptions deliveryOptions = new DeliveryOptions();
    private boolean hasError;
    private String errorMessage;

    private Envelope() {
    }

    public static Envelope whenComplete(Object data, Throwable throwable) {
        final Envelope envelope = data(data);
        if (throwable != null) {
            envelope.hasError = true;
            envelope.errorMessage = throwable.getLocalizedMessage();
        }
        return envelope;
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

    public static Envelope error(Throwable throwable) {
        final Envelope envelope = new Envelope();
        envelope.hasError = true;
        envelope.errorMessage = throwable.getLocalizedMessage();
        return envelope;
    }

    public void reply(Message<Object> reply) {
        if (hasError) {
            reply.fail(400, errorMessage);
        } else {
            reply.reply(data, deliveryOptions);
        }
    }

    public Envelope putHeader(String name, String value) {
        deliveryOptions.addHeader(name, value);
        return this;
    }
}
