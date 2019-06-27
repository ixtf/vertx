package com.github.ixtf.jax.rs.demo.verticle;

import com.github.ixtf.vertx.CorsConfig;
import com.github.ixtf.vertx.Jvertx;
import com.github.ixtf.vertx.ws.rs.JaxRsRouteResolver;
import com.google.common.collect.Sets;
import io.reactivex.Completable;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;

import java.util.Set;

/**
 * @author jzb 2019-05-02
 */
public class AgentVerticle extends AbstractVerticle {
    @Override
    public Completable rxStart() {
        final Router router = Jvertx.router(vertx, new CorsConfig());

        final Redis redis = Redis.createClient(vertx, new RedisOptions());

//        router.get("/test").handler(rc -> vertx.eventBus().<String>rxSend("test", null)
//                .map(Message::body)
//                .subscribe(rc.response()::end, rc::fail));

        router.get("/test").handler(rc -> {
            vertx.eventBus().send("test", "test");
            vertx.eventBus().publish("test", "test");
            rc.response().end();
        });

        Jvertx.resolve(AgentResolver.class).forEach(it -> it.router(router));
        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        return vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .rxListen(8080)
                .ignoreElement();
    }

    public static class AgentResolver extends JaxRsRouteResolver {

        @Override
        protected Set<String> getPackages() {
            return Sets.newHashSet("com.github.ixtf.jax.rs.demo");
        }

        @Override
        protected Set<Class> getClasses() {
            return null;
        }
    }
}
