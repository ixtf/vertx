package vietj;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * @author jzb 2019-02-16
 */
public class ConfigureThreadPool {
    public static void eventLoop() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(10));
    }

    public static void worker() {
        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(10));
    }
}
