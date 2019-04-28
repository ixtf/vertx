package spi;

import com.google.inject.ImplementedBy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * @author jzb 2019-04-28
 */
@ImplementedBy(IRestImpl.class)
@Path("/IRest")
@Produces(APPLICATION_JSON)
public interface IRest {
    @Produces(TEXT_PLAIN)
    @GET
    String get();
}
