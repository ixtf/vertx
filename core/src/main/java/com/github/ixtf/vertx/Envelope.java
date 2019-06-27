package com.github.ixtf.vertx;

import com.github.ixtf.japp.core.J;
import io.reactivex.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import lombok.Getter;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-28
 */
public class Envelope {
    @Getter
    private final Object data;
    @Getter
    private final DeliveryOptions deliveryOptions;

    public Envelope(Object data, DeliveryOptions deliveryOptions) {
        this.data = data;
        this.deliveryOptions = deliveryOptions;
    }

    public Envelope(Envelope envelope) {
        this(envelope.data, envelope.deliveryOptions);
    }

    public Envelope(Object data) {
        this(data, new DeliveryOptions());
    }

    public static Mono<Object> toMessage(Object o) {
        if (o == null || o instanceof String || o instanceof byte[]) {
            return Mono.justOrEmpty(o);
        }
        if (o instanceof ClusterSerializable) {
            final ClusterSerializable clusterSerializable = (ClusterSerializable) o;
            final Buffer buffer = Buffer.buffer();
            clusterSerializable.writeToBuffer(buffer);
            return Mono.justOrEmpty(buffer.getBytes());
        }
        if (o instanceof Envelope) {
            final Envelope envelope = (Envelope) o;
            return envelope.toMessage();
        }
        if (o instanceof Optional) {
            final Optional optional = (Optional) o;
            return Mono.justOrEmpty(optional).flatMap(Envelope::toMessage);
        }
        if (o instanceof Mono) {
            final Mono mono = (Mono) o;
            return mono.flatMap(Envelope::toMessage);
        }
        if (o instanceof Flux) {
            final Flux flux = (Flux) o;
            return flux.collectList().flatMap(Envelope::toMessage);
        }
        if (o instanceof CompletionStage) {
            final CompletionStage completionStage = (CompletionStage) o;
            return Mono.fromCompletionStage(completionStage).flatMap(Envelope::toMessage);
        }
        if (o instanceof Completable) {
            final Completable completable = (Completable) o;
            return Mono.from(completable.toFlowable()).flatMap(Envelope::toMessage);
        }
        if (o instanceof Single) {
            final Single single = (Single) o;
            return Mono.from(single.toFlowable()).flatMap(Envelope::toMessage);
        }
        if (o instanceof Maybe) {
            final Maybe maybe = (Maybe) o;
            return Mono.from(maybe.toFlowable()).flatMap(Envelope::toMessage);
        }
        if (o instanceof Flowable) {
            final Flowable flowable = (Flowable) o;
            return Flux.from(flowable).collectList().flatMap(Envelope::toMessage);
        }
        if (o instanceof Observable) {
            final Observable observable = (Observable) o;
            final Flowable<Collection> flowable = observable.toList().toFlowable();
            return Mono.from(flowable).flatMap(it -> {
                if (J.isEmpty(it)) {
                    return Mono.empty();
                }
                if (it.size() == 1) {
                    return Flux.fromIterable(it).single();
                }
                return Mono.justOrEmpty(it);
            }).flatMap(Envelope::toMessage);
        }
        return Mono.fromCallable(() -> MAPPER.writeValueAsString(o));
    }

    @SneakyThrows
    public static void main(String[] args) {
        Mono.empty().defaultIfEmpty("").subscribe(it -> {
            System.out.println("test1");
            System.out.println(it);
        });
        Mono.from(Completable.complete().toFlowable()).subscribe(it -> {
            System.out.println("test2");
            System.out.println(it);
        });
        System.out.println("end");
        Thread.sleep(Duration.ofDays(1).toMillis());
    }

    public Envelope putHeader(String name, String value) {
        deliveryOptions.addHeader(name, value);
        return this;
    }

    public Mono<Object> toMessage() {
        return toMessage(data);
    }

}
