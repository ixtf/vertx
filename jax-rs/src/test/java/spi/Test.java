package spi;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.Vertx;

/**
 * @author jzb 2019-02-28
 */
public class Test {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        INJECTOR = Guice.createInjector(new TestModule());

//        vertx.deployVerticle(new AbstractVerticle());
        vertx.rxDeployVerticle(WorkerVerticle.class.getName(), new DeploymentOptions().setWorker(true).setInstances(32)).subscribe();
        vertx.rxDeployVerticle(AgentVerticle.class.getName(), new DeploymentOptions().setInstances(32)).subscribe();
    }
}
