package com.amazonaws.xray.opentelemetry.tracing;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.opentelemetry.tracing.propagation.HttpTraceContext;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;

/**
 * A tracer backed by an AWS X-Ray recorder.
 *
 * @see io.opentelemetry.trace.Tracer
 */
public class RecorderBackedTracer implements Tracer {

  private static final HttpTraceContext HTTP_TRACE_CONTEXT = new HttpTraceContext();
  private AWSXRayRecorder recorder;
  private Span currentSpan = null;
  private Entity currentEntity = null;

  private RecorderBackedTracer(AWSXRayRecorder recorder) {
    this.recorder = recorder;
  }

  /**
   * Create a tracer using a given recorder.
   *
   * @param recorder the recorder
   * @return a tracer
   */
  public static RecorderBackedTracer fromRecorder(AWSXRayRecorder recorder) {
    return new RecorderBackedTracer(recorder);
  }

  /**
   * {@inheritDoc}
   * This implementation will automatically update the active span based on changes to the X-Ray
   * recorder but otherwise conforms to OpenTelemetry semantics when creating spans.
   */
  @Override
  public Span getCurrentSpan() {
    if (currentSpan == null) {
      return DefaultSpan.getInvalid();
    } else {
      //Reflect the recorder's current entity changing in response to X-Ray calls
      Entity recorderCurrentEntity = recorder.getTraceEntity();
      if (recorderCurrentEntity != null && !recorderCurrentEntity.equals(currentEntity)) {
        currentEntity = recorderCurrentEntity;
        currentSpan = EntitySpan.fromEntity(currentEntity);
      }
    }
    return currentSpan;
  }

  @Override
  public Scope withSpan(final Span span) {
    if (span instanceof EntitySpan) {
      final EntitySpan adapter = (EntitySpan) span;
      currentSpan = span;
      recorder.setTraceEntity(adapter.getXrayEntity());
    }

    //Just a rough implementation. There's some talk of separating scope management from
    //existing gRPC influenced deps. Need to better integrate this and X-Ray Recorder's
    //scope managerment.
    return new Scope() {
      @Override
      public void close() {
      }
    };
  }

  @Override
  public Span.Builder spanBuilder(final String name) {
    return EntitySpanBuilder.create(name, recorder, getCurrentSpan());
  }

  @Override
  public BinaryFormat<SpanContext> getBinaryFormat() {
    return null;
  }

  @Override
  public HttpTextFormat<SpanContext> getHttpTextFormat() {
    return HTTP_TRACE_CONTEXT;
  }
}
