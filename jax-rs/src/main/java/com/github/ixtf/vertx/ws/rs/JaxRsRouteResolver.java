package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.vertx.RepresentationResolver;
import com.github.ixtf.vertx.route.RouteRepresentation;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import javax.ws.rs.Path;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class JaxRsRouteResolver extends RepresentationResolver<RouteRepresentation> {

    @Override
    public Collection<? extends RouteRepresentation> resolve() {
        @Cleanup final ScanResult scanResult = new ClassGraph().enableAllInfo()
                .whitelistPackages(ArrayUtils.nullToEmpty(getPackages()))
                .whitelistClasses(ArrayUtils.nullToEmpty(getClasses()))
                .scan();
        return scanResult.getClassesWithAnnotation(Path.class.getName())
                .filter(classInfo -> {
                    if (classInfo.isInterface()) {
                        return true;
                    }
                    final ClassInfoList interfaces = classInfo.getInterfaces();
                    if (interfaces.size() == 0) {
                        return true;
                    }
                    return false;
                })
                .filter(this::classInfoFilter)
                .loadClasses().parallelStream()
                .map(JaxRsResource::new)
                .flatMap(JaxRsResource::routes)
                .peek(it -> log.info("address=" + it.getAddress()))
                .collect(toList());
    }

    protected abstract String[] getPackages();

    protected String[] getClasses() {
        return new String[0];
    }

}
