package adv;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Context;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.Vertx;

import java.util.concurrent.ForkJoinPool;

/**
 * @author jzb 2019-02-16
 */
public class ContextTest {
    public static void main(String[] args) throws InterruptedException {
//        test1();
        test2();
    }

    private static void test2() throws InterruptedException {
        final Vertx vertx = Vertx.vertx();


        vertx.eventBus().consumer("test2", reply -> reply.reply(""));
        vertx.runOnContext(v -> {
            // On the event loop
            System.out.println("Calling blocking block from " + Thread.currentThread());

            Handler<Future<String>> blockingCodeHandler = future -> {
                // Non event loop
                System.out.println("Computing with " + Thread.currentThread());

                // Running on context from the worker
                vertx.runOnContext(v2 -> {
                    System.out.println("Running on context from the worker " + Thread.currentThread());
                });
            };

            // Execute the blocking code handler and the associated result handler
            vertx.executeBlocking(blockingCodeHandler, result -> {
            });
        });

        Thread.sleep(1000);
        ForkJoinPool.commonPool().submit(() -> {
            vertx.eventBus().rxSend("test2", "test")
                    .subscribe(it -> {
                        System.out.println(Context.isOnVertxThread());
                        System.out.println(Context.isOnEventLoopThread());
                        System.out.println(Context.isOnWorkerThread());
                    });
//            System.out.println(Context.isOnVertxThread());
//            System.out.println(Context.isOnEventLoopThread());
//            System.out.println(Context.isOnWorkerThread());
        });
    }

    private static void test1() {
        final Vertx vertx = Vertx.vertx();
        System.out.println("Current context is " + Vertx.currentContext());
        vertx.deployVerticle(new AbstractVerticle() {
            @Override
            public void start() throws Exception {
                System.out.println("Current context is " + Vertx.currentContext());
                System.out.println("Verticle context is " + context);
                System.exit(0);
            }
        }, new DeploymentOptions().setWorker(true));
    }

}
