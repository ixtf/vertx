package com.github.ixtf.jax.rs.demo;

import com.github.ixtf.persistence.mongo.Jmongo;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.vertx.reactivex.core.Vertx;

/**
 * @author jzb 2019-05-02
 */
public class DemoModule extends AbstractModule {
    public static Injector INJECTOR;
    private final Vertx vertx;

    public DemoModule(Vertx vertx) {
        this.vertx = vertx;
    }

    @Provides
    @Singleton
    private Jmongo jmongo() {
        return Jmongo.of(JmongoDev.class);
    }

    @Provides
    private Vertx Vertx() {
        return vertx;
    }
}
