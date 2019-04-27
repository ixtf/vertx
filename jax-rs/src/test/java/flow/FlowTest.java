package flow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.IntStream;

/**
 * @author jzb 2019-02-16
 */
public class FlowTest {
    //    public static final int COUNT = 10_000;
    public static final int COUNT = 10000;

    public static void main(String[] args) throws InterruptedException {
        final MySubscriber subscriber = new MySubscriber();

        final SubmissionPublisher<Object> publisher1 = new SubmissionPublisher<>();
        publisher1.subscribe(subscriber);
        final SubmissionPublisher<Object> publisher2 = new SubmissionPublisher<>();
        publisher2.subscribe(subscriber);

        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        final Runnable runnable = () -> {
            IntStream.rangeClosed(1, 1000).forEach(publisher1::submit);
            IntStream.rangeClosed(1, 1000).forEach(publisher2::submit);
        };
        IntStream.rangeClosed(1, 10).parallel().forEach(it -> {
            executorService.submit(runnable);
            System.out.println("job-" + it);
        });

//        IntStream.rangeClosed(1, COUNT).parallel().forEach(publisher::submit);
//        final int counter = subscriber.await().getCounter();
//        publisher.close();
//        System.out.println(counter);

        Thread.sleep(20_000);

        System.out.println("result = " + subscriber.getCounter1());
        System.out.println("result = " + subscriber.getCounter2().get());
    }

}
