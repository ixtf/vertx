package com.github.ixtf.jax.rs.demo.rest.internal;

import com.github.ixtf.jax.rs.demo.application.OperatorCreateCommand;
import com.github.ixtf.jax.rs.demo.domain.Operator;
import com.github.ixtf.jax.rs.demo.rest.OperatorApi;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.MongoUnitOfWork;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-05-02
 */
@Singleton
public class OperatorResource implements OperatorApi {
    private final Jmongo jmongo;

    @Inject
    private OperatorResource(Jmongo jmongo) {
        this.jmongo = jmongo;
    }

    @Override
    public void asyncTest() {
    }

    @Override
    public Operator create(OperatorCreateCommand command) {
        @NotBlank final String hrId = command.getHrId();
        final Mono<Operator> byHrId = jmongo.find(Operator.class, eq("hrId", hrId));
        final String oaId = command.getOaId();
        final Mono<Operator> byOaId = jmongo.find(Operator.class, eq("oaId", oaId));
        final Operator operator = byHrId.switchIfEmpty(byOaId).defaultIfEmpty(new Operator()).block();
        operator.setHrId(hrId);
        operator.setOaId(oaId);
        operator.setName(command.getName());
        operator.setId("test");
        final MongoUnitOfWork uow = jmongo.uow();
        uow.registerNew(operator);
        uow.rxCommit().then();
        return operator;
    }

    @Override
    public Mono list(@Min(0) int first, @Min(1) @Max(1000) int limit) {
        final Map<String, Object> map = Maps.newConcurrentMap();
        map.put("first", first);
        map.put("limit", limit);
        return jmongo.count(Operator.class).flatMap(count -> {
            map.put("count", count);
            return jmongo.query(Operator.class, first, limit).collect(toList());
        }).map(operators -> {
            map.put("operators", operators);
            return map;
        });
    }
}
