package com.github.ixtf.jax.rs.demo;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.jax.rs.demo.verticle.WorkerVerticle;
import com.google.inject.Guice;
import io.reactivex.Completable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.github.ixtf.jax.rs.demo.DemoModule.INJECTOR;

/**
 * @author jzb 2019-05-02
 */
@Slf4j
public class Worker {

    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            INJECTOR = Guice.createInjector(new DemoModule(vertx));

            return Completable.mergeArray(
                    vertx.rxDeployVerticle(WorkerVerticle.class.getName(), new DeploymentOptions().setWorker(true)).ignoreElement()
            );
        }).doOnError(err -> log.error("", err)).subscribe();
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
