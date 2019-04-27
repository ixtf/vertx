package path;

import com.fasterxml.jackson.databind.node.TextNode;
import com.github.ixtf.vertx.jax_rs.api.JaxRs;

/**
 * @author jzb 2019-02-20
 */
public class PathTest {
    public static void main(String[] args) {
        final String path = JaxRs.vertxPath("api/sd", "/tasks/{taskId}/ops/:opId");
        System.out.println(path);

        System.out.println(TextNode.valueOf("123").asInt());
    }
}
