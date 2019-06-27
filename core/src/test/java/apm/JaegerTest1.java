package apm;

import com.github.ixtf.vertx.apm.RCTextMapInjectAdapter;
import com.google.common.collect.ImmutableMap;
import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.vertx.core.eventbus.DeliveryOptions;

import static io.opentracing.propagation.Format.Builtin.TEXT_MAP;

/**
 * docker run --rm -d --name jaeger   -e COLLECTOR_ZIPKIN_HTTP_PORT=9411   -p 5775:5775/udp   -p 6831:6831/udp   -p 6832:6832/udp   -p 5778:5778   -p 16686:16686   -p 14268:14268   -p 9411:9411   jaegertracing/all-in-one
 *
 * @author jzb 2019-06-25
 */
public class JaegerTest1 {
    private final Tracer tracer;

    private JaegerTest1(Tracer tracer) {
        this.tracer = tracer;
    }

    public static void main(String[] args) {
        final JaegerTracer tracer = initTracer("JaegerTest1");

        final Span span = tracer.buildSpan("api")
                .withTag(Tags.SPAN_KIND, Tags.SPAN_KIND_PRODUCER)
                .withTag(Tags.MESSAGE_BUS_DESTINATION, "address")
                .start();
        tracer.inject(span.context(), TEXT_MAP, new RCTextMapInjectAdapter(new DeliveryOptions()));
//        tracer.inject(span.context(), HTTP_HEADERS, new RCTextMapInjectAdapter(new DeliveryOptions()));


        new JaegerTest1(tracer).sayHello("world");
    }

    public static JaegerTracer initTracer(String service) {
        Configuration.SamplerConfiguration samplerConfig = Configuration.SamplerConfiguration.fromEnv().withType("const").withParam(1);
        Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(true);
        Configuration config = new Configuration(service).withSampler(samplerConfig).withReporter(reporterConfig);
        return config.getTracer();
    }

    private void sayHello(String helloTo) {
        Span span = tracer.buildSpan("say-hello").start();
        span.setTag("hello-to", helloTo);

        String helloStr = String.format("Hello, %s!", helloTo);
        System.out.println(helloStr);
        span.log(ImmutableMap.of("event", "println"));

        span.finish();
    }
}
