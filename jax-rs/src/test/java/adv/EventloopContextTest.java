package adv;

import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;

/**
 * @author jzb 2019-02-16
 */
public class EventloopContextTest {
    public static void main(String[] args) {
        System.out.println(Thread.currentThread());
        final VertxOptions options = new VertxOptions()
                .setEventLoopPoolSize(2);
        Vertx vertx = Vertx.vertx(options);
        for (int i = 0; i < 20; i++) {
            int index = i;
            vertx.setTimer(1, timerID -> {
                final Thread thread = Thread.currentThread();
                System.out.println(index + ":" + thread.toString());
            });
        }
    }
}
