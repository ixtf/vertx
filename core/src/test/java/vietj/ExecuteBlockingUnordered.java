package vietj;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * @author jzb 2019-02-16
 */
public class ExecuteBlockingUnordered {
    Handler<Future<String>> blockingCodeHandler1 = future -> {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("blockingCodeHandler1");
    };
    Handler<Future<String>> blockingCodeHandler2 = future -> {
        System.out.println("blockingCodeHandler2");
    };
    Handler<Future<String>> blockingCodeHandler3 = future -> {
        System.out.println("blockingCodeHandler3");
    };

    public static void main(String[] args) throws Exception {
        new ExecuteBlockingUnordered().execute(Vertx.vertx());
    }

    public void execute(Vertx vertx) {
        vertx.runOnContext(v -> {

            // The blocks are executed on any available worker thread
            vertx.executeBlocking(blockingCodeHandler1, ar -> {
            });
            vertx.executeBlocking(blockingCodeHandler2, ar -> {
            });
            vertx.executeBlocking(blockingCodeHandler3, ar -> {
            });
        });
    }
}
