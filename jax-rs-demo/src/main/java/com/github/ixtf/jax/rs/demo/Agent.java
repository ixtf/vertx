package com.github.ixtf.jax.rs.demo;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.jax.rs.demo.verticle.AgentVerticle;
import com.google.inject.Guice;
import io.reactivex.Completable;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2019-05-02
 */
public class Agent {

    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            DemoModule.INJECTOR = Guice.createInjector(new DemoModule(vertx));

            return Completable.mergeArray(
                    vertx.rxDeployVerticle(AgentVerticle.class.getName()).ignoreElement()
            );
        }).subscribe();
    }

    private static VertxOptions vertxOptions() {
        final VertxOptions vertxOptions = new VertxOptions()
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(10));
        Optional.ofNullable(System.getProperty("vertx.cluster.host"))
                .filter(J::nonBlank)
                .ifPresent(vertxOptions.getEventBusOptions()::setHost);
        return vertxOptions;
    }
}
