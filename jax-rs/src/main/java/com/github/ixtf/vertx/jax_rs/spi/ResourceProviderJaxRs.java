package com.github.ixtf.vertx.jax_rs.spi;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.vertx.api.ResourceRepresentation;
import com.github.ixtf.vertx.jax_rs.api.ResourceRepresentationJaxRs;
import com.github.ixtf.vertx.spi.ResourceProvider;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

/**
 * @author jzb 2019-02-20
 */
@Slf4j
public abstract class ResourceProviderJaxRs implements ResourceProvider {
    protected ClassLoader classLoader;
    protected ClassPath classPath;
    private Set<ResourceRepresentation> resourceRepresentations;

    @Override
    public Stream<ResourceRepresentation> listResources() {
        if (resourceRepresentations != null) {
            return resourceRepresentations.stream();
        }
        synchronized (ResourceProviderJaxRs.class) {
            if (resourceRepresentations != null) {
                return resourceRepresentations.stream();
            }
            final Stream<? extends Class<?>> packageStream = J.emptyIfNull(getPackages()).stream()
                    .map(classPath()::getTopLevelClassesRecursive)
                    .flatMap(Collection::parallelStream)
                    .map(ClassPath.ClassInfo::load);
            resourceRepresentations = Stream.concat(packageStream, J.emptyIfNull(getClasses()).stream())
                    .filter(defaultFilter().and(classFilter()))
                    .collect(toSet())
                    .parallelStream()
                    .map(it -> new ResourceRepresentationJaxRs(this, it))
                    .peek(resourceRepresentation -> resourceRepresentation.routes().forEach(it -> {
                        log.debug("api=" + it.getPath() + "\t address=" + it.getAddress());
                    }))
                    .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
        }
        return resourceRepresentations.stream();
    }

    protected abstract Set<String> getPackages();

    protected abstract Set<Class> getClasses();

    protected Predicate<Class> classFilter() {
        return clazz -> true;
    }

    @SneakyThrows
    protected synchronized ClassPath classPath() {
        if (classPath == null) {
            classPath = ClassPath.from(classLoader());
        }
        return classPath;
    }

    protected synchronized ClassLoader classLoader() {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    private Predicate<Class> defaultFilter() {
        final Collection<String> excludePrefixes = ImmutableSet.of("java.", "javax.", "com.sun.");
        return clazz -> {
            if (clazz.getAnnotation(Path.class) == null) {
                return false;
            }
            final String packageName = clazz.getPackageName();
            for (String prefix : excludePrefixes) {
                if (StringUtils.startsWith(packageName, prefix)) {
                    return false;
                }
            }
            return true;
        };
    }

}
