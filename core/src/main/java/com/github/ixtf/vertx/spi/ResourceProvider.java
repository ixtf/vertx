package com.github.ixtf.vertx.spi;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.api.ResourceRepresentation;
import io.vertx.reactivex.ext.auth.User;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author jzb 2019-02-20
 */
public interface ResourceProvider {

    default String addressPrefix() {
        return "japp-vertx";
    }

    default Optional<String> principal(RoutingContext rc) {
        return Optional.ofNullable(rc.user())
                .map(User::principal)
                .map(it -> it.getString("uid"))
                .filter(J::nonBlank);
    }

    Stream<ResourceRepresentation> listResources();

    <T> T getProxy(Class<T> resourceClass);
}
