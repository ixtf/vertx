package com.github.ixtf.jax.rs.demo;

import com.github.ixtf.persistence.mongo.Jmongo;
import com.google.inject.*;
import io.vertx.core.Vertx;

/**
 * @author jzb 2019-05-02
 */
public class DemoModule extends AbstractModule {
    public static Injector INJECTOR;
    private final Vertx vertx;

    private DemoModule(Vertx vertx) {
        this.vertx = vertx;
    }

    static void init(Vertx vertx) {
        INJECTOR = Guice.createInjector(new DemoModule(vertx));
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
