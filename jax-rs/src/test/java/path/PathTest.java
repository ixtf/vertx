package path;

import com.github.ixtf.vertx.ws.rs.JaxRs;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jzb 2019-02-20
 */
public class PathTest {
    @Test
    public void test1() {
        final String path = JaxRs.vertxPath("api/sd", "/tasks/{taskId}/ops/:opId");
        Assert.assertEquals(path, "/api/sd/tasks/:taskId/ops/:opId");
    }
}
