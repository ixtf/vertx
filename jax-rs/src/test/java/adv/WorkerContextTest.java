package adv;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;

/**
 * @author jzb 2019-02-16
 */
public class WorkerContextTest {
    public static void main(String[] args) throws InterruptedException {
        final VertxOptions vertxOptions = new VertxOptions()
                .setWorkerPoolSize(2);
        Vertx vertx = Vertx.vertx(vertxOptions);
        final DeploymentOptions deploymentOptions = new DeploymentOptions()
                .setWorker(true)
                .setWorkerPoolSize(10)
                .setInstances(4);
        vertx.deployVerticle(TheWorker.class.getName(), deploymentOptions);

        Thread.sleep(2000);

// Send 10 messages
        send(vertx, 10);
    }

    private static void send(Vertx vertx, int count) {
        for (int i = 0; i < count; i++) {
            vertx.eventBus().send("the-address", "");
        }
    }

    public static class TheWorker extends AbstractVerticle {
        @Override
        public void start() throws Exception {
            vertx.eventBus().consumer("the-address", msg -> {
                try {
                    Thread.sleep(10);
                    System.out.println("Executed by " + Thread.currentThread());
                    msg.reply("whatever");
                } catch (InterruptedException e) {
                    msg.fail(0, "Interrupted");
                }
            });
        }
    }
}
