package smallrye;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

/**
 * @author jzb 2019-02-16
 */
public class DataProcessor extends AbstractVerticle {

    private static final int PORT = 8080;

    @Override
    public void start(Future<Void> done) {
        vertx.createHttpServer().requestHandler(request -> {
            // Consume messages from the Vert.x event bus
            MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer("data");
            // Wrap the stream and manipulate the data
            ReactiveStreams.fromPublisher(consumer.toFlowable())
                    .limit(5) // Take only 5 messages
                    .map(Message::body) // Extract the body
                    .map(json -> json.getInteger("value")) // Extract the value
//                            .peek(i -> System.out.println("Got value: " + i)) // Print it
//                            .peek(i -> System.out.println(Thread.currentThread())) // Print it
                    .reduce(0, (acc, value) -> acc + value)
                    .run() // Begin to receive items
                    .whenComplete((res, err) -> {
                        System.out.println("test   " + Thread.currentThread());
                        // When the 5 items has been consumed, write the result to the
                        // HTTP response:
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (err != null) {
                            request.response().setStatusCode(500).end(err.getMessage());
                        } else {
                            request.response().end("Result is: " + res);
                        }
                    });
        }).listen(PORT, ar -> done.handle(ar.mapEmpty()));
    }

}

