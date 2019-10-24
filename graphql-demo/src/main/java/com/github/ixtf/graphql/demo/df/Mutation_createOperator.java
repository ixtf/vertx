package com.github.ixtf.graphql.demo.df;

import com.github.ixtf.vertx.graphql.GraphQLArg;
import com.github.ixtf.vertx.graphql.GraphQLEndPoint;
import com.github.ixtf.vertx.graphql.GraphQLMutation;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.Map;

/**
 * @author jzb 2019-10-05
 */
@GraphQLEndPoint
public class Mutation_createOperator {
    @GraphQLMutation("createOperator")
    public Mono<Map<String, Object>> createOperator(@GraphQLArg("command") Map<String, Object> command) {
        return Mono.fromCallable(() -> {
            System.out.println(command);
            return Map.of("id", "id", "name", command.get("name"), "birthDate", new Date(), "cdt", new Date());
        }).subscribeOn(Schedulers.elastic());
    }
}
