package com.github.ixtf.jax.rs.demo.rest;

import com.github.ixtf.jax.rs.demo.application.OperatorCreateCommand;
import com.github.ixtf.jax.rs.demo.domain.Operator;
import com.github.ixtf.jax.rs.demo.rest.internal.OperatorResource;
import com.google.inject.ImplementedBy;
import reactor.core.publisher.Mono;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.*;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-04-28
 */
@ImplementedBy(OperatorResource.class)
@Path("operators")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface OperatorApi {

    @Path("asyncTest")
    @GET
    Mono<Void> asyncTest();

    @POST
    Operator create(OperatorCreateCommand command);

    @GET
    Mono<Map> list(@QueryParam("first") @Min(0) int first,
                   @QueryParam("pageSize") @DefaultValue("50") @Min(1) @Max(1000) int limit);
}
