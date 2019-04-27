package adv;

import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jzb 2019-02-16
 */
public class S {
    public static void main(String[] args) throws InterruptedException {
        final Vertx vertx = Vertx.vertx();
        System.out.println("Current context is " + Vertx.currentContext());
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                AtomicLong count = new AtomicLong(10);
                long now = System.currentTimeMillis();
                System.out.println("Starting periodic on " + Thread.currentThread());
                vertx.setPeriodic(1000, id -> {
                    if (count.decrementAndGet() < 0) {
                        vertx.cancelTimer(id);
                        vertx.rxClose().subscribe(() -> {
                            System.exit(0);
                        });
                    }
                    System.out.println("Periodic fired " + Thread.currentThread() + " after " + (System.currentTimeMillis() - now) + " ms");
                });
            }

            @Override
            public void stop() throws Exception {
                System.out.println("stop ");
            }
        }, new DeploymentOptions().setWorker(true));

        Thread.sleep(2000);
// Send 10 messages
        send(vertx, 10);
    }

    private static void send(Vertx vertx, int count) {
        for (int i = 0; i < count; i++) {
            vertx.eventBus().send("the-address", "");
        }
    }
}
