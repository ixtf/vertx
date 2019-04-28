package spi;

import com.github.ixtf.vertx.ws.rs.JaxRsRouteEBResolver;
import com.google.common.collect.Sets;

import java.util.Set;

import static spi.Test.INJECTOR;

/**
 * @author jzb 2019-02-28
 */
public class RouteEBResolver extends JaxRsRouteEBResolver {
    @Override
    protected Set<String> getPackages() {
        return null;
    }

    @Override
    protected Set<Class> getClasses() {
        return Sets.newHashSet(TestResource.class,IRest.class);
    }

    @Override
    protected Object getProxy(Class<?> clazz) {
        return INJECTOR.getInstance(clazz);
    }
}
