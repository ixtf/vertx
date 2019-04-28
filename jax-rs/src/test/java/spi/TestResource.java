package spi;

import com.github.ixtf.vertx.util.Envelope;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.*;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * @author jzb 2019-02-28
 */
@Singleton
@Path("")
@Produces(APPLICATION_JSON)
public class TestResource {
    @Path("getTest")
    @GET
    @Produces(TEXT_PLAIN)
    public String test() {
        return "test";
    }

    @Path("pathParamTest1/{id123}")
    @GET
    @Produces(TEXT_PLAIN)
    public String test(@PathParam("id123") String id) {
        return "pathParamTest1: " + id;
    }

    @Path("pathParamTest2/{id}")
    @GET
    @Produces(TEXT_PLAIN)
    public String test(@PathParam("id") long id) {
        return "pathParamTest2: " + id;
    }

    @Path("ReactiveStreams1/{id}")
    @GET
    public CompletionStage<Envelope> ReactiveStreams1(@PathParam("id") String id) {
        return ReactiveStreams.of("ReactiveStreams1", id)
                .toList()
                .run()
                .thenApply(Envelope::data);
    }

    @Path("queryParamTest1")
    @GET
    public CompletionStage<Envelope> queryParamTest1(@QueryParam("string") @NotBlank String string,
                                                     @QueryParam("boolean") boolean b,
                                                     @QueryParam("Boolean") Boolean B,
                                                     @QueryParam("stringDefault") @DefaultValue("stringDefault") String stringDefault) {
        final Map result = Maps.newHashMap();
        result.put("boolean", b);
        result.put("Boolean", B);
        result.put("string", string);
        result.put("stringDefault", stringDefault);
        result.put("currentThread", Thread.currentThread().toString());
//        result.put("ld", ld);
        return ReactiveStreams.of(result)
                .map(Envelope::data)
                .map(it -> it.putHeader("header-test", "header-test"))
                .toList()
                .run()
                .thenApply(it -> it.get(0));
    }

    @Path("queryParamTest2")
    @GET
    public Publisher<Envelope> queryParamTest2(@QueryParam("string") String string,
                                               @QueryParam("boolean") boolean b,
                                               @QueryParam("Boolean") Boolean B,
                                               @QueryParam("stringDefault") @DefaultValue("stringDefault") String stringDefault) {
        final Map result = Maps.newHashMap();
        result.put("boolean", b);
        result.put("Boolean", B);
        result.put("string", string);
        result.put("stringDefault", stringDefault);
        return ReactiveStreams.of(result)
                .map(Envelope::data)
                .buildRs();
    }
}
