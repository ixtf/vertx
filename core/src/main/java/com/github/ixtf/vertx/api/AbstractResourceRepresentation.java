package com.github.ixtf.vertx.api;

import com.github.ixtf.vertx.spi.ResourceProvider;
import lombok.Getter;

/**
 * @author jzb 2019-02-14
 */
public abstract class AbstractResourceRepresentation implements ResourceRepresentation {
    @Getter
    protected final ResourceProvider resourceProvider;
    @Getter
    protected final Class<?> resourceClass;

    protected AbstractResourceRepresentation(ResourceProvider resourceProvider, Class<?> resourceClass) {
        this.resourceProvider = resourceProvider;
        this.resourceClass = resourceClass;
    }

    public String addressPrefix() {
        return resourceProvider.addressPrefix();
    }
}