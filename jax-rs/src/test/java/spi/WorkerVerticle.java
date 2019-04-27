package spi;

import com.github.ixtf.vertx.Jvertx;
import io.reactivex.Completable;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * @author jzb 2019-02-28
 */
public class WorkerVerticle extends AbstractVerticle {
    @Override
    public Completable rxStart() {
        final Completable other$ = Completable.mergeArray(
                vertx.eventBus().consumer("test-bytes", reply -> {
                    final Buffer buffer = Buffer.buffer("test-bytes", UTF_8.name());
                    reply.reply("test-bytes".getBytes(UTF_8));
                }).rxCompletionHandler()
        );

        Jvertx.resolve(RouteEBResolver.class).forEach(it -> it.consumer(vertx));
        return Completable.mergeArray(other$);
    }

}
