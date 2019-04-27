package spi;

import com.github.ixtf.vertx.RCReplyEnvelope;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import javax.ws.rs.*;
import java.time.LocalDate;
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
    public CompletionStage<RCReplyEnvelope> ReactiveStreams1(@PathParam("id") String id) {
        return ReactiveStreams.of("ReactiveStreams1", id)
                .toList()
                .run()
                .thenApply(RCReplyEnvelope::data);
    }

    @Path("queryParamTest1")
    @GET
    public CompletionStage<RCReplyEnvelope> queryParamTest1(@QueryParam("string") String string,
                                                            @QueryParam("boolean") boolean b,
                                                            @QueryParam("Boolean") Boolean B,
                                                            @QueryParam("stringDefault") @DefaultValue("stringDefault") String stringDefault,
                                                            @QueryParam("ld") LocalDate ld) {
        final Map result = Maps.newHashMap();
        result.put("boolean", b);
        result.put("Boolean", B);
        result.put("string", string);
        result.put("stringDefault", stringDefault);
        result.put("ld", ld);
        return ReactiveStreams.of(result)
                .map(RCReplyEnvelope::data)
                .toList()
                .run()
                .thenApply(it -> it.get(0));
    }

    @Path("queryParamTest2")
    @GET
    public Publisher<RCReplyEnvelope> queryParamTest2(@QueryParam("string") String string,
                                                      @QueryParam("boolean") boolean b,
                                                      @QueryParam("Boolean") Boolean B,
                                                      @QueryParam("stringDefault") @DefaultValue("stringDefault") String stringDefault,
                                                      @QueryParam("ld") LocalDate ld) {
        final Map result = Maps.newHashMap();
        result.put("boolean", b);
        result.put("Boolean", B);
        result.put("string", string);
        result.put("stringDefault", stringDefault);
        result.put("ld", ld);
        return ReactiveStreams.of(result)
                .map(RCReplyEnvelope::data)
                .buildRs();
    }
}
