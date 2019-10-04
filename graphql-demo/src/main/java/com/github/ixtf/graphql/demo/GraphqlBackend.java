package com.github.ixtf.graphql.demo;

import graphql.GraphQL;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import static com.github.ixtf.graphql.demo.AgentModule.INJECTOR;

/**
 * @author jzb 2019-09-23
 */
@Slf4j
public class GraphqlBackend extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final GraphQL graphQL = INJECTOR.getInstance(GraphQL.class);
        super.start(startFuture);
    }
}
