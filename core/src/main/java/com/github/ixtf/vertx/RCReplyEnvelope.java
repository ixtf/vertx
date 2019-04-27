package com.github.ixtf.vertx;

import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.SneakyThrows;

import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-28
 */
public class RCReplyEnvelope {
    private Object data;
    private DeliveryOptions deliveryOptions = new DeliveryOptions();
    private boolean hasError;
    private String errorMessage;

    private RCReplyEnvelope() {
    }

    public static RCReplyEnvelope whenComplete(Object data, Throwable throwable) {
        final RCReplyEnvelope envelope = data(data);
        if (throwable != null) {
            envelope.hasError = true;
            envelope.errorMessage = throwable.getLocalizedMessage();
        }
        return envelope;
    }

    @SneakyThrows
    public static RCReplyEnvelope data(Object data) {
        if (data instanceof RCReplyEnvelope) {
            return (RCReplyEnvelope) data;
        }
        if (data instanceof Optional) {
            final Optional optional = (Optional) data;
            return RCReplyEnvelope.data(optional.orElse(null));
        }
        final RCReplyEnvelope envelope = new RCReplyEnvelope();
        if (data == null) {
            envelope.data = null;
        } else if (data instanceof String || data instanceof byte[]) {
            envelope.data = data;
        } else {
            envelope.data = MAPPER.writeValueAsString(data);
        }
        return envelope;
    }

    public static RCReplyEnvelope error(Throwable throwable) {
        final RCReplyEnvelope envelope = new RCReplyEnvelope();
        envelope.hasError = true;
        envelope.errorMessage = throwable.getLocalizedMessage();
        return envelope;
    }

    public void reply(Message reply) {
        if (hasError) {
            reply.fail(400, errorMessage);
        } else {
            reply.reply(data, deliveryOptions);
        }
    }

    public RCReplyEnvelope putHeader(String name, String value) {
        deliveryOptions.addHeader(name, value);
        return this;
    }
}
