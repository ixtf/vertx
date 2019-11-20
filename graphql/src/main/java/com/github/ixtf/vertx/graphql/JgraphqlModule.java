package com.github.ixtf.vertx.graphql;

import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import graphql.language.TypeDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.idl.FieldWiringEnvironment;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.WiringFactory;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.ext.web.handler.graphql.VertxPropertyDataFetcher;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author jzb 2019-10-05
 */
public class JgraphqlModule extends AbstractModule {

    protected RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .scalar(Jgraphql.getGraphQLLocalDate())
                .scalar(Jgraphql.getGraphQLLocalDateTime())
                .scalar(Jgraphql.getGraphQLJson())
                .wiringFactory(new WiringFactory() {
                    @Override
                    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
                        return new VertxPropertyDataFetcher(environment.getFieldDefinition().getName());
                    }

                    @Override
                    public boolean providesDataFetcher(FieldWiringEnvironment environment) {
                        final TypeDefinition parentType = environment.getParentType();
                        return Objects.equals("Query", parentType.getName()) || Objects.equals("Mutation", parentType.getName());
                    }

                    @Override
                    public DataFetcher getDataFetcher(FieldWiringEnvironment environment) {
                        return new VertxDataFetcher<>((env, promise) -> {
                            final RoutingContext rc = env.getContext();
                            final Mono<String> address$ = Mono.fromCallable(() -> {
                                final String pName = env.getParentType().getName();
                                final String fName = env.getField().getName();
                                return "graphql:" + pName + ":" + fName;
                            }).subscribeOn(Schedulers.elastic());
                            final Mono<String> message$ = Mono.fromCallable(rc::getBodyAsString).subscribeOn(Schedulers.elastic());
                            final Mono<DeliveryOptions> deliveryOptions$ = Mono.fromCallable(() -> new DeliveryOptions()).subscribeOn(Schedulers.elastic());
                            Mono.zip(address$, message$, deliveryOptions$).subscribeOn(Schedulers.elastic())
                                    .map(tuple3 -> Future.<Message<Object>>future(p -> rc.vertx().eventBus().request(tuple3.getT1(), tuple3.getT2(), tuple3.getT3(), p)))
                                    .subscribe(message -> message.map(Message::body).setHandler(promise), promise::fail);
                        });
                    }
                }).build();
    }

    @SneakyThrows(IOException.class)
    protected String loadSdl(String fileName) {
        final URL url = Resources.getResource(fileName);
        return Resources.toString(url, UTF_8);
    }
}
