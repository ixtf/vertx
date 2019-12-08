package com.github.ixtf.jax.rs.demo.verticle;

import com.github.ixtf.vertx.CorsConfig;
import com.github.ixtf.vertx.Jvertx;
import com.github.ixtf.vertx.ws.rs.JaxRsRouteResolver;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;

import static com.github.ixtf.jax.rs.demo.DemoModule.INJECTOR;

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

        Jvertx.resolve(AgentResolver.class).forEach(it -> it.router(router, INJECTOR::getInstance));
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
        protected String[] getPackages() {
            return new String[]{"com.github.ixtf.jax.rs.demo"};
        }

    }
}
