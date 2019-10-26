package com.github.ixtf.jax.rs.demo.verticle;

import com.github.ixtf.jax.rs.demo.domain.Operator;
import com.github.ixtf.jax.rs.demo.verticle.AgentVerticle.AgentResolver;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.vertx.Jvertx;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.List;

import static com.github.ixtf.jax.rs.demo.DemoModule.INJECTOR;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-05-02
 */
public class WorkerVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start();
        vertx.eventBus().consumer("test", reply -> {
            final Jmongo jmongo = INJECTOR.getInstance(Jmongo.class);

            jmongo.find(Operator.class, "5b384b2cd8712064f101e31e")
                    .doOnSuccess(operator -> {
                        operator.setName("mes-admin");
                        System.out.println("operator = " + operator);
                    })
                    .subscribe();
        });

        final List<Future> futures = Jvertx.resolve(AgentResolver.class)
                .map(it -> it.consumer(vertx, INJECTOR::getInstance))
                .collect(toList());
        CompositeFuture.all(futures).<Void>mapEmpty().setHandler(startFuture);
    }

}
