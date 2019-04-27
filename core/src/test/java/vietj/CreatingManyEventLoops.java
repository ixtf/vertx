package vietj;

import io.vertx.core.Vertx;

import java.util.concurrent.CountDownLatch;

/**
 * @author jzb 2019-02-16
 */
public class CreatingManyEventLoops {
    public static void main(String[] args) throws Exception {
        System.out.println(Thread.currentThread());
        Vertx vertx = Vertx.vertx();
        for (int i = 0; i < 20; i++) {
            int index = i;
            CountDownLatch latch = new CountDownLatch(1);
            vertx.setTimer(1, id -> {
                System.out.println(index + ":" + Thread.currentThread());
                latch.countDown();
            });
//            latch.await(2, TimeUnit.SECONDS);
        }

        Thread.sleep(2000);
        System.exit(0);
    }
}
