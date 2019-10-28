package com.github.ixtf.graphql.demo;

import com.github.ixtf.japp.core.J;
import graphql.GraphQL;
import io.vertx.core.*;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.graphql.ApolloWSHandler;
import io.vertx.ext.web.handler.graphql.GraphQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandler;
import io.vertx.ext.web.handler.graphql.GraphiQLHandlerOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.github.ixtf.graphql.demo.AgentModule.INJECTOR;

/**
 * @author jzb 2019-09-23
 */
@Slf4j
public class GraphqlAgent extends AbstractVerticle {

    public static void main(String[] args) {
        Future.<Vertx>future(promise -> {
            final VertxOptions vertxOptions = new VertxOptions()
                    .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(10));
            Optional.ofNullable(System.getProperty("vertx.cluster.host")).filter(J::nonBlank)
                    .ifPresent(vertxOptions.getEventBusOptions()::setHost);
            Vertx.clusteredVertx(vertxOptions, promise);
        }).compose(vertx -> {
            AgentModule.init(vertx);
            return Future.<String>future(p -> {
                final DeploymentOptions deploymentOptions = new DeploymentOptions();
                vertx.deployVerticle(GraphqlAgent.class, deploymentOptions, p);
            });
        }).setHandler(ar -> {
            if (ar.succeeded()) {
                System.out.println("agent success");
            } else {
                ar.cause().printStackTrace();
            }
        });
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setUploadsDirectory(FileUtils.getTempDirectoryPath()));
        router.route().handler(ResponseContentTypeHandler.create());
        router.route().handler(CorsHandler.create("^http(s)?://localhost(:[1-9]\\d+)?")
                .allowedHeaders(Set.of("access-control-allow-origin", "origin", "content-type", "accept", "authorization"))
                .allowedMethods(Set.of(HttpMethod.values()))
        );

        final GraphQL graphQL = INJECTOR.getInstance(GraphQL.class);
        router.route("/graphql").handler(ApolloWSHandler.create(graphQL));
        router.route("/graphql").handler(GraphQLHandler.create(graphQL));

        final GraphiQLHandlerOptions options = new GraphiQLHandlerOptions().setEnabled(true);
        router.route("/graphiql/*").handler(GraphiQLHandler.create(options).graphiQLRequestHeaders(rc -> {
            String token = rc.get("token");
            return MultiMap.caseInsensitiveMultiMap().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }));

        router.route("/sync/:name").handler(rc -> {
            final String name = rc.pathParam("name");
            vertx.eventBus().<String>request("sync", name, reply -> {
                if (reply.succeeded()) {
                    rc.response().end(reply.result().body());
                } else {
                    rc.response().end(reply.cause().getMessage());
                }
            });
        });
        router.route("/async/:name").handler(rc -> {
            final String name = rc.pathParam("name");
            vertx.eventBus().<String>request("async", name, reply -> {
                if (reply.succeeded()) {
                    rc.response().end(reply.result().body());
                } else {
                    rc.response().end(reply.cause().getMessage());
                }
            });
        });

        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setCompressionSupported(true)
                .setWebsocketSubProtocols("graphql-ws");
        vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .listen(8080, ar -> startFuture.handle(ar.mapEmpty()));
    }
}
