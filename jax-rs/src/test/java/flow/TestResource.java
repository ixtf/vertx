package flow;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class TestResource implements IF1, IF2 {

    @Override
    public String get1_1(String test) {
        return null;
    }

    @Override
    public String test2() {
        return null;
    }

    public static void main(String[] args) {
        final Injector INJECTOR = Guice.createInjector(new TestModule());
        IF1 instance = INJECTOR.getInstance(IF1.class);
        System.out.println(instance);

        instance = INJECTOR.getInstance(IF1.class);
        System.out.println(instance);

        for (Class<?> anInterface : TestResource.class.getInterfaces()) {
            System.out.println(anInterface);
        }
        System.out.println();
    }
}
