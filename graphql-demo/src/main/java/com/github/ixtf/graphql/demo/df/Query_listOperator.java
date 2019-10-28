package com.github.ixtf.graphql.demo.df;

import com.github.ixtf.vertx.graphql.GraphQLArg;
import com.github.ixtf.vertx.graphql.GraphQLEndPoint;
import com.github.ixtf.vertx.graphql.GraphQLQuery;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author jzb 2019-10-05
 */
@GraphQLEndPoint
public class Query_listOperator {
    @GraphQLQuery("listOperator")
    public Mono<Map<String, Object>> listOperator(@GraphQLArg("first") Long first, @GraphQLArg("pageSize") Integer pageSize) {
        return Mono.fromCallable(() -> {
            final long count = 12345;
            final List<Map> operators = IntStream.range(0, 10).mapToObj(i ->
                    Map.of("id", i, "name", "name" + i, "birthDate", new Date(), "cdt", new Date())
            ).collect(Collectors.toList());
            return Map.of("first", first, "pageSize", pageSize, "count", count, "operators", operators);
        }).subscribeOn(Schedulers.elastic());
    }
}
