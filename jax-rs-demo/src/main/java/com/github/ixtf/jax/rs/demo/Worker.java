package com.github.ixtf.jax.rs.demo;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.jax.rs.demo.verticle.WorkerVerticle;
import io.vertx.core.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2019-05-02
 */
@Slf4j
public class Worker {

    public static void main(String[] args) {
        Future.<Vertx>future(promise -> {
            final VertxOptions vertxOptions = new VertxOptions()
                    .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(10));
            Optional.ofNullable(System.getProperty("vertx.cluster.host")).filter(J::nonBlank)
                    .ifPresent(vertxOptions.getEventBusOptions()::setHost);
            Vertx.clusteredVertx(vertxOptions, promise);
        }).compose(vertx -> {
            DemoModule.init(vertx);
            final Future<String> f1 = Future.future(promise -> {
                final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true).setInstances(10);
                vertx.deployVerticle(WorkerVerticle.class, deploymentOptions, promise);
            });
            return CompositeFuture.all(List.of(f1));
        }).setHandler(ar -> {
            if (ar.succeeded()) {
                log.info(Worker.class.getName());
            } else {
                log.error("", ar.cause());
            }
        });
    }

}
