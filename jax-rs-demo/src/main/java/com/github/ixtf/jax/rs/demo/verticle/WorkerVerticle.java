package com.github.ixtf.jax.rs.demo.verticle;

import com.github.ixtf.jax.rs.demo.domain.Operator;
import com.github.ixtf.jax.rs.demo.verticle.AgentVerticle.AgentResolver;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.vertx.Jvertx;
import io.vertx.core.AbstractVerticle;

import static com.github.ixtf.jax.rs.demo.DemoModule.INJECTOR;

/**
 * @author jzb 2019-05-02
 */
public class WorkerVerticle extends AbstractVerticle {
    @Override
    public void start() {
        vertx.eventBus().consumer("test", reply -> {
            final Jmongo jmongo = INJECTOR.getInstance(Jmongo.class);

            jmongo.find(Operator.class, "5b384b2cd8712064f101e31e")
                    .doOnSuccess(operator -> {
                        operator.setName("mes-admin");
                        System.out.println("operator = " + operator);
                    })
                    .subscribe();
        });

        Jvertx.resolve(AgentResolver.class).forEach(it -> it.consumer(vertx, INJECTOR::getInstance));
    }

}
