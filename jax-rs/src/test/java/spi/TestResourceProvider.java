package spi;

import com.github.ixtf.vertx.jax_rs.spi.ResourceProviderJaxRs;
import com.google.common.collect.Sets;

import java.util.Set;

import static spi.Test.INJECTOR;

/**
 * @author jzb 2019-02-28
 */
public class TestResourceProvider extends ResourceProviderJaxRs {
    @Override
    protected Set<String> getPackages() {
        return null;
    }

    @Override
    protected Set<Class> getClasses() {
        return Sets.newHashSet(TestResource.class);
    }

    @Override
    public <T> T getProxy(Class<T> resourceClass) {
        return INJECTOR.getInstance(resourceClass);
    }
}
