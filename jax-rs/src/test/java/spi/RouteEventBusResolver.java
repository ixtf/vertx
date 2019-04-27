package spi;

import com.github.ixtf.vertx.ws.rs.JaxRsRouteResolver;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author jzb 2019-02-28
 */
public class RouteEventBusResolver extends JaxRsRouteResolver {
    @Override
    protected Set<String> getPackages() {
        return null;
    }

    @Override
    protected Set<Class> getClasses() {
        return Sets.newHashSet(TestResource.class);
    }

}
