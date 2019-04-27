package com.github.ixtf.vertx;

import com.github.ixtf.japp.core.J;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public abstract class RepresentationResolver<T> {
    protected ClassLoader classLoader;
    protected ClassPath classPath;

    protected abstract Set<String> getPackages();

    protected abstract Set<Class> getClasses();

    protected abstract Stream<? extends T> resolve();

    protected Stream<Class> classes() {
        final Collection<String> excludePrefixes = ImmutableSet.of("java.", "javax.", "com.sun.");
        final var packageStream = J.emptyIfNull(getPackages()).parallelStream()
                .map(classPath()::getTopLevelClassesRecursive)
                .flatMap(Collection::parallelStream)
                .map(ClassPath.ClassInfo::load);
        return Stream.concat(packageStream, J.emptyIfNull(getClasses()).parallelStream()).filter(clazz -> {
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
        });
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

}
