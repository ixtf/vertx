package apm;

import com.google.common.collect.ImmutableMap;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;

/**
 * @author jzb 2019-06-25
 */
public class JaegerTest4 {

    public static void main(String[] args) {
        new JaegerTest4().sayHello("world");
    }

    public static JaegerTracer initTracer(String service) {
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1);
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
        Configuration config = new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }

    private void sayHello(String helloTo) {
        final JaegerTracer tracer = initTracer("sayHello");
        Span span = tracer.buildSpan("say-hello").start();
        span.setTag("hello-to", helloTo);
        span.finish();

        String helloStr = formatString(span, helloTo);
        printHello(span, helloStr);
    }

    private String formatString(Span rootSpan, String helloTo) {
        final JaegerTracer tracer = initTracer("formatString");
        Span span = tracer.buildSpan("formatString").asChildOf(rootSpan).start();
        try {
            String helloStr = String.format("Hello, %s!", helloTo);
            span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
            return helloStr;
        } finally {
            span.finish();
        }
    }

    private void printHello(Span rootSpan, String helloStr) {
        final JaegerTracer tracer = initTracer("printHello");
        Span span = tracer.buildSpan("printHello").asChildOf(rootSpan).start();
        try {
            System.out.println(helloStr);
            span.log(ImmutableMap.of("event", "println"));
        } finally {
            span.finish();
        }
    }
}
