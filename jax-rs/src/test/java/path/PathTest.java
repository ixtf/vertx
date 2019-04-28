package path;

import com.github.ixtf.vertx.ws.rs.JaxRs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author jzb 2019-02-20
 */
@DisplayName("Vertx Path Test")
public class PathTest {
    @Test
    public void test1() {
        final String path = JaxRs.vertxPath("api/sd", "/tasks/{taskId}/ops/:opId");
        assertEquals(path, "/api/sd/tasks/:taskId/ops/:opId");
    }

    @Test
    public void test2() {
        final String path = JaxRs.vertxPath("api/sd/", "/tasks/{taskId}/ops/:opId");
        assertEquals(path, "/api/sd/tasks/:taskId/ops/:opId");
    }
}
