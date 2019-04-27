package vertx;

import com.hazelcast.config.Config;
import io.reactivex.Completable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2019-02-19
 */
@Slf4j
public class Agent extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            final DeploymentOptions options = new DeploymentOptions();
            return vertx.rxDeployVerticle(Agent.class.getName(), options).ignoreElement();
        }).subscribe();
    }

    @SneakyThrows
    private static VertxOptions vertxOptions() {
        final Config config = new Config();
        config.getGroupConfig().setName("vertx-test-cluster");
        final HazelcastClusterManager hazelcastClusterManager = new HazelcastClusterManager(config);

        return new VertxOptions()
                .setClusterManager(hazelcastClusterManager)
                .setClusterHost(InetAddress.getLocalHost().getHostAddress())
//                .setWorkerPoolSize(10_000)
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(6))
                .setMaxWorkerExecuteTime(TimeUnit.HOURS.toNanos(1));
    }

    @Override
    public Completable rxStart() {
        final Router router = Router.router(vertx);
        router.route(HttpMethod.GET, "/sendByAsyn").handler(this::sendByAsyn);
        router.route(HttpMethod.GET, "/sendByRx").handler(this::sendByRx);
        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(8080)
                .ignoreElement();
    }

    private void sendByAsyn(RoutingContext rc) {
        vertx.eventBus().<String>send("vertx-test", null, ar -> {
            System.out.println("sendByAsyn reply: " + Thread.currentThread());
            if (ar.succeeded()) {
                final Message<String> message = ar.result();
                rc.response().end(message.body());
            } else {
                rc.fail(ar.cause());
            }
        });
    }

    private void sendByRx(RoutingContext rc) {
        final String q = rc.request().getParam("q");
        System.out.println("aST[" + q + "]: " + LocalTime.now() + "  " + Thread.currentThread());
        final long start = System.currentTimeMillis();

        vertx.eventBus().<String>rxSend("vertx-test", q)
                .subscribe(it -> {
                    rc.response().end(it.body());
                    final long end = System.currentTimeMillis();
                    System.out.println("aED[" + q + "]: " + LocalTime.now() + " [" + (end - start) + "] " + Thread.currentThread());
                }, rc::fail);
        System.out.println("aAsync");
    }
}
