package com.github.ixtf.jax.rs.demo.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-04-26
 */
@Path("")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public interface CURDResource {

    @Operation(tags = "auth", summary = "获取", description = "sdfsf")
    @ApiResponse(responseCode = "200", description = "ok", content = @Content(schema = @Schema(implementation = TResult.class)))
    @ApiResponse(responseCode = "400", ref = "#/components/responses/JException")
    @GET
    TResult get(@QueryParam("test") @NotBlank String test);
}
