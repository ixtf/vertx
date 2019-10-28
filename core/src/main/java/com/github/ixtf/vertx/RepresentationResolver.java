package com.github.ixtf.vertx;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class RepresentationResolver<T> {
    protected ClassLoader classLoader;
    protected ClassPath classPath;

    protected abstract Set<String> getPackages();

    protected abstract Set<Class> getClasses();

    public abstract Stream<? extends T> resolve();

    protected Stream<Class> classStream() {
        final Collection<String> excludePrefixes = ImmutableSet.of("java.", "javax.", "com.sun.");
        final Stream<? extends Class<?>> packageStream = J.emptyIfNull(getPackages()).parallelStream()
                .map(classPath()::getTopLevelClassesRecursive)
                .flatMap(Collection::parallelStream)
                .map(ClassPath.ClassInfo::load);
        return Stream.concat(packageStream, J.emptyIfNull(getClasses()).parallelStream()).parallel()
                .distinct()
                .filter(clazz -> {
                    final String packageName = clazz.getPackage().getName();
                    for (String prefix : excludePrefixes) {
                        if (StringUtils.startsWith(packageName, prefix)) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(classFilter());
    }

    @SneakyThrows(IOException.class)
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

    protected Predicate<Class> classFilter() {
        return clazz -> true;
    }

}
