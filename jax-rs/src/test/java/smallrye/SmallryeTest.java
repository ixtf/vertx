package smallrye;

import io.vertx.reactivex.core.Vertx;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

/**
 * @author jzb 2019-02-16
 */
public class SmallryeTest {
    public static void main(String[] args) {
        ReactiveStreams.of("hello", "from", "smallrye", "reactive", "stream", "operators")
                .map(String::toUpperCase) // Transform the words
                .filter(s -> s.length() > 4) // Filter items
                .forEach(word -> System.out.println(">> " + word)) // Terminal operation
                .run();

//        ReactiveStreams.ofNullable("testsss")
//                .peek(it->{
//                    try {
//                        Thread.sleep(10_000);
//                    } catch (InterruptedException e) {
//                    }
//                })
//                .findFirst()
//                .run();
//                .whenComplete((it, error) -> {
//                    try {
//                        Thread.sleep(10_000);
//                    } catch (InterruptedException e) {
//                    }
//
//                    final Object test = it.orElse("test");
//                    System.out.println(test);
//                });

        System.out.println("end");

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(DataGenerator.class.getName());
        vertx.deployVerticle(DataProcessor.class.getName());
    }
}
