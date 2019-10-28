package apm;

import com.google.common.collect.ImmutableMap;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;

/**
 * @author jzb 2019-06-25
 */
public class JaegerTest3 {
    private final Tracer tracer;

    private JaegerTest3(Tracer tracer) {
        this.tracer = tracer;
    }

    public static void main(String[] args) {
        final JaegerTracer tracer = initTracer("JaegerTest3");
        new JaegerTest3(tracer).sayHello("world");
    }

    public static JaegerTracer initTracer(String service) {
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1);
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
        Configuration config = new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }

    private void sayHello(String helloTo) {
        final Span span = tracer.buildSpan("say-hello").start();
        try (Scope scope = tracer.activateSpan(span)) {
            span.setTag("hello-to", helloTo);

            String helloStr = formatString(helloTo);
            printHello(helloStr);
        } finally {
            span.finish();
        }
    }

    private String formatString(String helloTo) {
        final Span span = tracer.buildSpan("formatString").start();
        try (Scope scope = tracer.activateSpan(span)) {
            String helloStr = String.format("Hello, %s!", helloTo);
            span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
            return helloStr;
        } finally {
            span.finish();
        }
    }

    private void printHello(String helloStr) {
        final Span span = tracer.buildSpan("printHello").start();
        try (Scope scope = tracer.activateSpan(span)) {
            System.out.println(helloStr);
            span.log(ImmutableMap.of("event", "println"));
        } finally {
            span.finish();
        }
    }
}
