package flow;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;

/**
 * @author jzb 2019-02-16
 */
public class AgentVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        final Router router = Router.router(vertx);

        router.get("/test").handler(rc -> {
            final HttpServerRequest request = rc.request();
            final HttpServerResponse response = rc.response();
            vertx.eventBus().<Buffer>send("test", "", ar -> {
                ar.succeeded();
                final Message<Buffer> result = ar.result();
                response.end(result.body());
            });
        });

        vertx.createHttpServer().requestHandler(router);
    }
}
