package flow;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.reactivestreams.ReactiveWriteStream;

import java.util.stream.IntStream;

/**
 * @author jzb 2019-02-16
 */
public class EbTest {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(WorkerVerticle.class.getName(), new DeploymentOptions().setWorker(true).setInstances(10));
        vertx.deployVerticle(AgentVerticle.class.getName(), new DeploymentOptions().setInstances(10));
        IntStream.rangeClosed(1, 10).forEach(i -> {
            final String message = "message-" + i;
            vertx.eventBus().send("test", message, ar -> {
                final ReactiveWriteStream<Object> writeStream = ReactiveWriteStream.writeStream(vertx);
                writeStream.setWriteQueueMaxSize(1);
            });
        });
    }
}
