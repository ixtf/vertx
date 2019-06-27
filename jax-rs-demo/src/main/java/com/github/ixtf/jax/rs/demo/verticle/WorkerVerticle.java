package com.github.ixtf.jax.rs.demo.verticle;

import com.github.ixtf.jax.rs.demo.domain.Operator;
import com.github.ixtf.jax.rs.demo.verticle.AgentVerticle.AgentResolver;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.vertx.Jvertx;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;

import java.util.Collection;

import static com.github.ixtf.jax.rs.demo.DemoModule.INJECTOR;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-05-02
 */
public class WorkerVerticle extends AbstractVerticle {
    @Override
    public Completable rxStart() {
        vertx.eventBus().consumer("test", reply -> {
            final Jmongo jmongo = INJECTOR.getInstance(Jmongo.class);

            jmongo.find(Operator.class, "5b384b2cd8712064f101e31e")
                    .doOnSuccess(operator -> {
                        operator.setName("mes-admin");
                        System.out.println("operator = " + operator);
                    })
                    .subscribe();
//            reply.reply("test");

//            jmongo.find(Operator.class, "5b384b2cd8712064f101e31e")
//                    .ignoreElement()
//                    .doOnSuccess(it -> {
//                        System.out.println("doOnSuccess: " + it);
//                    })
//                    .subscribe(it -> {
//                        System.out.println(it);
//                    });
//            Single.fromPublisher(jmongo.find(Operator.class, "5b384b2cd8712064f101e31e"))
//                    .subscribe(it -> {
//                        System.out.println("Single: " + Thread.currentThread());
//                    });
//            jmongo.find(Operator.class, "5b384b2cd8712064f101e31e")
//                    .subscribe(operator -> {
//                        System.out.println("Mono: " + Thread.currentThread());
//                        reply.reply(operator.getName());
//                    }, err -> reply.fail(400, err.getLocalizedMessage()));
        });

        final Collection<Completable> consumers$ = Jvertx.resolve(AgentResolver.class)
                .map(it -> it.consumer(vertx, INJECTOR::getInstance))
                .collect(toList());
        return Completable.merge(consumers$);
    }

}
