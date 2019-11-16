package com.github.ixtf.vertx;

import io.github.classgraph.ClassInfo;

import java.util.Collection;

public abstract class RepresentationResolver<T> {

    protected String name() {
        return "API";
    }

    protected abstract Collection<? extends T> resolve();

    protected boolean classInfoFilter(ClassInfo classInfo) {
        return true;
    }

}
