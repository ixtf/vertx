package spi;

import com.github.ixtf.vertx.Jvertx;
import io.reactivex.Completable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;

/**
 * @author jzb 2019-02-28
 */
public class AgentVerticle extends AbstractVerticle {

    @Override
    public Completable rxStart() {
        final Router router = Router.router(vertx);
        Jvertx.enableCommon(router);
        router.route().failureHandler(Jvertx::failureHandler);

        router.route(HttpMethod.GET, "/bytesTest").handler(rc -> {
            vertx.eventBus().rxSend("test-bytes", null).subscribe(message -> {
                final HttpServerResponse response = rc.response();
                System.out.println(message);
                response.end();
            }, rc::fail);
        });

        Jvertx.resolve(TestRouteResolver.class).forEach(it -> it.router(router));
        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setDecompressionSupported(true)
                .setCompressionSupported(true);
        return vertx.createHttpServer(httpServerOptions)
                .requestHandler(router)
                .rxListen(8080)
                .ignoreElement();
    }
}
