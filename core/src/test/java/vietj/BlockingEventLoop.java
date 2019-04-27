package vietj;

import io.vertx.core.Vertx;

/**
 * @author jzb 2019-02-16
 */
public class BlockingEventLoop {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.setTimer(1, id -> {
            try {
                Thread.sleep(7000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        });
    }
}
