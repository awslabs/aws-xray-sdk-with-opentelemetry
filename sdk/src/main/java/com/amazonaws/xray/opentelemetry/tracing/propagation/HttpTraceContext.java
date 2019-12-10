package com.amazonaws.xray.opentelemetry.tracing.propagation;

import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.opentelemetry.tracing.utils.ContextUtils;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.SpanContext;
import java.util.Collections;
import java.util.List;

/**
 * Propagate trace ID information via X-Ray Headers.
 *
 * @see io.opentelemetry.context.propagation.HttpTextFormat
 */
public class HttpTraceContext implements HttpTextFormat<SpanContext> {

  public static final String XRAY_HEADER_NAME = TraceHeader.HEADER_KEY;
  public static final List<String> FIELDS = Collections.singletonList(XRAY_HEADER_NAME);
  public static final io.opentelemetry.trace.propagation.HttpTraceContext OT_TRACE_CONTEXT
      = new io.opentelemetry.trace.propagation.HttpTraceContext();

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  //TODO - Also emit w3c headers
  public <C> void inject(final SpanContext value, final C carrier, final Setter<C> setter) {
    setter.put(carrier, XRAY_HEADER_NAME, ContextUtils.spanContextToHeader(value).toString());
  }

  @Override
  //TODO - Accept w3c headers
  public <C> SpanContext extract(final C carrier, final Getter<C> getter) {
    String xrayHeader = getter.get(carrier, XRAY_HEADER_NAME);
    if (xrayHeader != null) {
      return ContextUtils.headerToSpanContext(TraceHeader.fromString(xrayHeader));
    } else {
      throw new RuntimeException("Could not extract X-Ray Trace Header.");
    }
  }
}
