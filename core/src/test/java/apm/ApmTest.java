package apm;

import com.google.common.collect.Maps;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.mock.MockTracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;

import java.util.Map;

/**
 * @author jzb 2019-06-25
 */
public class ApmTest {
    public static void main(String[] args) {
        final Tracer tracer = new MockTracer();
//        final Tracer tracer = GlobalTracer.get();


        Span span = tracer.buildSpan("foo").start();
        span.setTag(Tags.COMPONENT, "my-own-application");

        tracer.scopeManager().activate(span);

        final Map<String, String> map = Maps.newHashMap();
        final TextMapAdapter textMapAdapter = new TextMapAdapter(map);
        tracer.inject(tracer.activeSpan().context(), Format.Builtin.TEXT_MAP, textMapAdapter);

        final SpanContext spanContext = tracer.extract(Format.Builtin.TEXT_MAP, textMapAdapter);


        final Span span1 = tracer.buildSpan("bar")
                .asChildOf(spanContext)
                .start();
        span1.log("test");
//        Tags.MESSAGE_BUS_DESTINATION.set(tracer.activeSpan(),);
        span1.finish();

        span.finish();

        // Inspect the Span's tags.
//        Map<String, Object> tags = span.tags();
//        System.out.println("tags = " + tags);

        System.out.println(span);
        System.out.println(span1);
        System.out.println(tracer);
    }
}
