package flow;

import com.google.inject.ImplementedBy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("")
@Produces(APPLICATION_JSON)
@ImplementedBy(TestResource.class)
public interface IF1 {
    @GET
    String get1_1(@QueryParam("test") String test);
}
