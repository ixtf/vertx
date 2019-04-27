package vertx;

import com.github.ixtf.persistence.mongo.Jmongo;
import com.hazelcast.config.Config;
import io.reactivex.Completable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import vertx.domain.SilkCar;

import java.net.InetAddress;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-02-19
 */
@Slf4j
public class Worker extends AbstractVerticle {
    public static void main(String[] args) {
        Vertx.rxClusteredVertx(vertxOptions()).flatMapCompletable(vertx -> {
            final DeploymentOptions options = new DeploymentOptions().setWorker(true);
            return vertx.rxDeployVerticle(Worker.class.getName(), options).ignoreElement();
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
//                .setWorkerPoolSize(1000)
                .setMaxEventLoopExecuteTime(TimeUnit.SECONDS.toNanos(6))
                .setMaxWorkerExecuteTime(TimeUnit.HOURS.toNanos(1));
    }

    @Override
    public Completable rxStart() {
        return vertx.eventBus().<String>consumer("vertx-test", reply -> {
//            final Optional<SilkCar> silkCar = Jmongo.findById(SilkCar.class, "5bfd4b4f67e7ad00013055df");
//            ObjectNode objectNode = silkCar.map(it -> MAPPER.convertValue(it, ObjectNode.class))
//                    .orElseGet(MAPPER::createObjectNode);
            final String q = reply.body();
            System.out.println("wST[" + q + "]: " + LocalTime.now() + "  " + Thread.currentThread());

            Jmongo.listAll(SilkCar.class).collect(toList()).run().thenAccept(silkCars -> {
                try {
                    reply.reply(MAPPER.writeValueAsString(silkCars));
                    System.out.println("wED[" + q + "]: " + LocalTime.now() + "  " + Thread.currentThread());
                } catch (Exception ex) {
                    reply.fail(400, "test");
                }
            });
            System.out.println("aAsync");

//            try {
//                final List<SilkCar> silkCars = Jmongo.listAll(SilkCar.class).collect(toList());
//                reply.reply(MAPPER.writeValueAsString(silkCars));
//                System.out.println("wED[" + q + "]: " + LocalTime.now() + "  " + Thread.currentThread());
//            } catch (Exception ex) {
//                reply.fail(400, "test");
//            }

//            System.out.println("before invoke");
//            final String invoke = ForkJoinPool.commonPool().invoke(new Task());
//            System.out.println("after invoke: " + invoke);
//
//            System.out.println("worker: " + Thread.currentThread());
//            reply.reply(objectNode.toString());
        }).rxCompletionHandler();
    }

    class Task extends RecursiveTask<String> {
        @Override
        protected String compute() {
            final List<SingleTask> tasks = IntStream.rangeClosed(1, 5).mapToObj(SingleTask::new).collect(toList());
            invokeAll(tasks);
            return "compute ok";
        }
    }

    @Data
    class SingleTask extends RecursiveTask<String> {
        private final int i;

        @Override
        protected String compute() {
            System.out.println("SingleTask[" + i + "]: " + Thread.currentThread() + " context: " + Vertx.currentContext());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
