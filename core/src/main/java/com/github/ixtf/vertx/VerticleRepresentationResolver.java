package com.github.ixtf.vertx;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public abstract class VerticleRepresentationResolver {
    protected ClassLoader classLoader;
    protected ClassPath classPath;

    protected abstract Set<String> getPackages();

    protected abstract Set<Class> getClasses();

    public abstract <T extends VerticleRepresentation> Stream<T> resolve();

    protected Stream<Class> classStream() {
        final Collection<String> excludePrefixes = ImmutableSet.of("java.", "javax.", "com.sun.");
        final Stream<? extends Class<?>> packageStream = J.emptyIfNull(getPackages()).parallelStream()
                .map(classPath()::getTopLevelClassesRecursive)
                .flatMap(Collection::parallelStream)
                .map(ClassPath.ClassInfo::load);
        return Stream.concat(packageStream, J.emptyIfNull(getClasses()).parallelStream())
                .parallel()
                .filter(clazz -> {
                    final String packageName = clazz.getPackage().getName();
                    for (String prefix : excludePrefixes) {
                        if (StringUtils.startsWith(packageName, prefix)) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(this::classFilter)
                .distinct();
    }

    @SneakyThrows
    synchronized protected ClassPath classPath() {
        if (classPath == null) {
            classPath = ClassPath.from(classLoader());
        }
        return classPath;
    }

    synchronized protected ClassLoader classLoader() {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    protected boolean classFilter(Class clazz) {
        return true;
    }

}
