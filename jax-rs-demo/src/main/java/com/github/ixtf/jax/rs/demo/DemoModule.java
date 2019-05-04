package com.github.ixtf.jax.rs.demo;

import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.JmongoOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
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
        return new Jmongo(new JmongoOptions() {
            private final MongoClient mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString("mongodb://192.168.0.38"))
                            .build()
            );

            @Override
            public MongoClient client() {
                return mongoClient;
            }

            @Override
            public String dbName() {
                return "mes-auto";
//                return "vertx-demo";
            }
        });
    }

    @Provides
    private Vertx Vertx() {
        return vertx;
    }
}
