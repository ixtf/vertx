package cluster;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.vertx.core.Future;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Queue;

/**
 * @author jzb 2019-09-02
 */
public class Cluster1 {
    public static void main(String[] args) {
        Future.future(promise -> Mono.fromCallable(() -> {
            if (1 == 1) {
//                throw new RuntimeException("err");
            }
            return "ok";
        }).subscribe(promise::complete, promise::fail)).setHandler(ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println(ar.result());
            }
        });

        // 创建一个 hazelcastInstance实例
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();
        // 创建集群Map
        Map<Integer, String> clusterMap = instance.getMap("MyMap");
        clusterMap.put(1, "Hello hazelcast map!");

        // 创建集群Queue
        Queue<String> clusterQueue = instance.getQueue("MyQueue");
        clusterQueue.offer("Hello hazelcast!");
        clusterQueue.offer("Hello hazelcast queue!");
    }
}
