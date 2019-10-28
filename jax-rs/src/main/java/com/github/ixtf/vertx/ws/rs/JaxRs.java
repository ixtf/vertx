package com.github.ixtf.vertx.ws.rs;

import com.github.ixtf.japp.core.J;
import org.apache.commons.lang3.ArrayUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author jzb 2019-02-20
 */
public final class JaxRs {

    private JaxRs() {
    }

    static String vertxPath(String parentPath, String subPath) {
        String result = concatePath(parentPath, subPath);
        final Pattern pattern = Pattern.compile("(\\{\\w+\\})", Pattern.DOTALL);
        Matcher m = pattern.matcher(result);
        while (m.find()) {
            final int start = m.start();
            final int end = m.end();
            final String replace = result.substring(start, end);
            final String pathParam = ":" + replace.substring(1, replace.length() - 1);
            result = result.replace(replace, pathParam);
            m = pattern.matcher(result);
        }
        return result;
    }

    private static String concatePath(String parentPath, String subPath) {
        parentPath = handlePath(parentPath);
        subPath = handlePath(subPath);
        if (J.isBlank(parentPath)) {
            if (J.isBlank(subPath)) {
                return "/";
            } else {
                return "/" + subPath;
            }
        } else {
            parentPath = "/" + parentPath;
            if (J.isBlank(subPath)) {
                return parentPath;
            } else {
                return parentPath + "/" + subPath;
            }
        }
    }

    private static String handlePath(String path) {
        path = J.deleteWhitespace(path);
        if (J.isBlank(path)) {
            return "";
        }
        final Pattern startP = Pattern.compile("(^/+)");
        final Matcher startM = startP.matcher(path);
        if (startM.find()) {
            final String start = startM.group(0);
            path = path.substring(start.length());
        }
        final Pattern endP = Pattern.compile("(/+$)");
        final Matcher endM = endP.matcher(path);
        if (endM.find()) {
            final String end = endM.group(0);
            path = path.substring(0, path.length() - end.length());
        }
        return path;
    }

    static String getPath(Class<?> clazz) {
        final Path annotation = clazz.getAnnotation(Path.class);
        return Optional.ofNullable(annotation)
                .map(Path::value)
                .map(J::deleteWhitespace)
                .orElse("");
    }

    static String getPath(Method method) {
        final Path annotation = method.getAnnotation(Path.class);
        return Optional.ofNullable(annotation)
                .map(Path::value)
                .map(J::deleteWhitespace)
                .orElse("");
    }

    static String[] getConsumes(Class<?> clazz) {
        final Consumes annotation = clazz.getAnnotation(Consumes.class);
        return Optional.ofNullable(annotation)
                .map(Consumes::value)
                .filter(ArrayUtils::isNotEmpty)
                .orElse(new String[0]);
    }

    static String[] getProduces(Class<?> clazz) {
        final Produces annotation = clazz.getAnnotation(Produces.class);
        return Optional.ofNullable(annotation)
                .map(Produces::value)
                .filter(ArrayUtils::isNotEmpty)
                .orElse(new String[0]);
    }

    static String[] getConsumes(Method method) {
        final Consumes annotation = method.getAnnotation(Consumes.class);
        return Optional.ofNullable(annotation)
                .map(Consumes::value)
                .filter(ArrayUtils::isNotEmpty)
                .orElse(new String[0]);
    }

    static String[] getProduces(Method method) {
        final Produces annotation = method.getAnnotation(Produces.class);
        return Optional.ofNullable(annotation)
                .map(Produces::value)
                .filter(ArrayUtils::isNotEmpty)
                .orElse(new String[0]);
    }

    static Predicate<Class> resourceFilter() {
        return clazz -> clazz.getAnnotation(Path.class) != null;
    }

    static Stream<Class> resourceStream(Class clazz) {
        final Class[] interfaces = clazz.getInterfaces();
        return ArrayUtils.isEmpty(interfaces) ? Stream.of(clazz) : Arrays.stream(interfaces);
    }

}
