package com.github.ixtf.jax.rs.demo.verticle;

import com.github.ixtf.vertx.CorsConfig;
import com.github.ixtf.vertx.Jvertx;
import com.github.ixtf.vertx.ws.rs.JaxRsRouteResolver;
import com.google.common.collect.Sets;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;

import java.util.Set;

/**
 * @author jzb 2019-05-02
 */
public class AgentVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
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
        Future.<HttpServer>future(promise -> vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .listen(8080, promise))
                .<Void>mapEmpty()
                .setHandler(startFuture);
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
