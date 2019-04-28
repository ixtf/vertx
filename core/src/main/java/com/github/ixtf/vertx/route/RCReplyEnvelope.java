package com.github.ixtf.vertx.route;

import com.github.ixtf.vertx.util.Envelope;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-28
 */
@Slf4j
abstract class RCReplyEnvelope {
    protected final Message reply;
    protected final RCEnvelope rcEnvelope;
    protected volatile DeliveryOptions deliveryOptions = new DeliveryOptions();

    protected RCReplyEnvelope(Message reply, RCEnvelope rcEnvelope) {
        this.reply = reply;
        this.rcEnvelope = rcEnvelope;
    }

    abstract void reply();

    void reply(Throwable err) {
        log.error("", err);
        reply.fail(400, err.getLocalizedMessage());
    }

    static RCReplyEnvelope create(Message reply, RCEnvelope rcEnvelope, Object ret) throws Exception {
        if (ret instanceof CompletionStage) {
            final CompletionStage completionStage = (CompletionStage) ret;
            return new RCReplyEnvelope_CompletionStage(reply, rcEnvelope, completionStage);
        }
        if (ret instanceof Future) {
            final Future future = (Future) ret;
            return RCReplyEnvelope.create(reply, rcEnvelope, future.get());
        }
        if (ret instanceof Completable) {
            final Completable completable = (Completable) ret;
            return new RCReplyEnvelope_Completable(reply, rcEnvelope, completable);
        }
        if (ret instanceof Maybe) {
            final Maybe maybe = (Maybe) ret;
            return new RCReplyEnvelope_Single(reply, rcEnvelope, maybe.toSingle());
        }
        if (ret instanceof Single) {
            final Single single = (Single) ret;
            return new RCReplyEnvelope_Single(reply, rcEnvelope, single);
        }
        if (ret instanceof Flowable) {
            final Flowable flowable = (Flowable) ret;
            return new RCReplyEnvelope_Single(reply, rcEnvelope, flowable.toList());
        }
        return new RCReplyEnvelope_Default(reply, rcEnvelope, ret);
    }

    protected Object toMessage(Object data) throws Exception {
        if (data == null || data instanceof String || data instanceof byte[]) {
            return data;
        }
        if (data instanceof Envelope) {
            final Envelope envelope = (Envelope) data;
            deliveryOptions = envelope.getDeliveryOptions();
            return toMessage(envelope.getData());
        }
        if (data instanceof Optional) {
            final Optional optional = (Optional) data;
            return toMessage(optional.orElse(null));
        }
        if (data instanceof JsonObject) {
            final JsonObject jsonObject = (JsonObject) data;
            return jsonObject.encode();
        }
        if (data instanceof JsonArray) {
            final JsonArray jsonArray = (JsonArray) data;
            return jsonArray.encode();
        }
        return MAPPER.writeValueAsString(data);
    }

    /**
     * @author jzb 2019-02-28
     */
    private static class RCReplyEnvelope_Completable extends RCReplyEnvelope {
        private final Completable completable;

        private RCReplyEnvelope_Completable(Message reply, RCEnvelope rcEnvelope, Completable completable) {
            super(reply, rcEnvelope);
            this.completable = completable;
        }

        @Override
        void reply() {
            completable.subscribe(() -> reply.reply(null, deliveryOptions), this::reply);
        }
    }

    private static class RCReplyEnvelope_Single extends RCReplyEnvelope {
        private final Single<?> single;

        private RCReplyEnvelope_Single(Message reply, RCEnvelope rcEnvelope, Single<?> single) {
            super(reply, rcEnvelope);
            this.single = single;
        }

        @Override
        void reply() {
            single.map(this::toMessage).subscribe(it -> reply.reply(it, deliveryOptions), this::reply);
        }
    }

    private static class RCReplyEnvelope_Default extends RCReplyEnvelope {
        private final Object message;

        public RCReplyEnvelope_Default(Message reply, RCEnvelope rcEnvelope, Object data) throws Exception {
            super(reply, rcEnvelope);
            this.message = toMessage(data);
        }

        @Override
        void reply() {
            reply.reply(message, deliveryOptions);
        }
    }

    private static class RCReplyEnvelope_CompletionStage extends RCReplyEnvelope {
        private final CompletionStage<?> completionStage;

        public RCReplyEnvelope_CompletionStage(Message reply, RCEnvelope rcEnvelope, CompletionStage completionStage) {
            super(reply, rcEnvelope);
            this.completionStage = completionStage;
        }

        @Override
        void reply() {
            completionStage.whenComplete((it, err) -> {
                if (err != null) {
                    reply(err);
                } else {
                    try {
                        final Object message = toMessage(it);
                        reply.reply(message, deliveryOptions);
                    } catch (Exception e) {
                        reply(e);
                    }
                }
            });
        }
    }
}
