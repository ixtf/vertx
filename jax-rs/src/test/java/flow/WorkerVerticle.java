package flow;

import io.vertx.core.AbstractVerticle;

/**
 * @author jzb 2019-02-16
 */
public class WorkerVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer("test", reply -> {
            System.out.println(Thread.currentThread());
            reply.reply(reply.body());
        });
    }
}
