package flow;

import lombok.Getter;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.atomic.AtomicInteger;

public class MySubscriber<T> implements Subscriber<T> {
    private Subscription subscription;
    @Getter
    private int counter1 = 0;
    @Getter
    private AtomicInteger counter2 = new AtomicInteger();

    @Override
    public void onSubscribe(Subscription subscription) {
        System.out.println("Subscribed");
        this.subscription = subscription;
        this.subscription.request(1); //requesting data from publisher
        System.out.println("onSubscribe requested 1 item");
    }

    @Override
    public void onNext(T item) {
        System.out.println(Thread.currentThread() + " Processing " + item);
        counter1++;
        counter2.incrementAndGet();
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable e) {
        System.out.println("Some error happened");
        e.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("All Processing Done");
    }

}

