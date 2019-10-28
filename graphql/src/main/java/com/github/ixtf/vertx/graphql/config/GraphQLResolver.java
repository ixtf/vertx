package com.github.ixtf.vertx.graphql.config;

import com.github.ixtf.vertx.VerticleRepresentationResolver;
import com.github.ixtf.vertx.graphql.GraphQLEndPoint;
import com.github.ixtf.vertx.graphql.GraphQLMutation;
import com.github.ixtf.vertx.graphql.GraphQLQuery;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.reflect.MethodUtils.getMethodsListWithAnnotation;

@Slf4j
public abstract class GraphQLResolver extends VerticleRepresentationResolver {
    private static Stream<Class> resourceStream(Class clazz) {
        final Class[] interfaces = clazz.getInterfaces();
        return ArrayUtils.isEmpty(interfaces) ? Stream.of(clazz) : Arrays.stream(interfaces);
    }

    private static Predicate<Class> resourceFilter() {
        return clazz -> clazz.getAnnotation(GraphQLEndPoint.class) != null;
    }

    @Override
    public Stream<GraphQLVerticleRepresentation> resolve() {
        return classStream().flatMap(GraphQLResolver::resourceStream)
                .filter(GraphQLResolver.resourceFilter())
                .collect(toSet()).parallelStream()
                .flatMap(this::routes)
                .peek(it -> log.debug("address=" + it.getAddress()));
    }

    protected abstract Injector injector();

    private Stream<GraphQLVerticleRepresentation> routes(Class<?> resourceClass) {
        Stream<GraphQLVerticleRepresentation> result = Stream.empty();
        Stream<GraphQLVerticleRepresentation> routeStream = getMethodsListWithAnnotation(resourceClass, GraphQLQuery.class).parallelStream()
                .map(it -> new GraphQLVerticleRepresentation(injector(), resourceClass, it));
        result = Stream.concat(result, routeStream);

        routeStream = getMethodsListWithAnnotation(resourceClass, GraphQLMutation.class).parallelStream()
                .map(it -> new GraphQLVerticleRepresentation(injector(), resourceClass, it));
        return Stream.concat(result, routeStream).parallel();
    }
}
