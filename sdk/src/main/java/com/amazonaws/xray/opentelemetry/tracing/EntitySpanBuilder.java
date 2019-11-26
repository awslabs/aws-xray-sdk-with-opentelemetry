package com.amazonaws.xray.opentelemetry.tracing;

import com.amazonaws.xray.AWSXRayRecorder;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import java.util.Map;

/**
 * A builder class which creates spans against a given X-Ray recorder.
 * @see io.opentelemetry.trace.Span.Builder
 */
public class EntitySpanBuilder implements Span.Builder {

    private final AWSXRayRecorder recorder;
    private final String name;

    private SpanContext parentContext;
    private Span parentSpan;
    private Span.Kind kind = Span.Kind.INTERNAL;
    private long startTimestamp;

    /**
     * Create a factory using the given recorder.
     * @param name the name of the span to create
     * @param recorder the recorder to use
     * @param currentSpan the current span or null
     * @return the builder
     */
    public static EntitySpanBuilder create(final String name, final AWSXRayRecorder recorder, final Span currentSpan) {
        return new EntitySpanBuilder(name, recorder, currentSpan);
    }

    private EntitySpanBuilder(final String name, final AWSXRayRecorder recorder, final Span currentSpan) {
        this.name = name;
        this.recorder = recorder;
        this.parentSpan = currentSpan;
    }

    @Override
    public Span.Builder setParent(final Span span) {
        this.parentSpan = span;
        this.parentContext = null;
        return this;
    }

    @Override
    public Span.Builder setParent(final SpanContext spanContext) {
        this.parentContext = spanContext;
        this.parentSpan = null;
        return this;
    }

    @Override
    public Span.Builder setNoParent() {
        parentContext = null;
        parentSpan = null;
        return this;
    }

    /*
     * Segment linking is not supported by X-Ray
     */
    @Override
    public Span.Builder addLink(final SpanContext spanContext) {
        return this;
    }

    @Override
    public Span.Builder addLink(final SpanContext spanContext, final Map<String, AttributeValue> map) {
        return this;
    }

    @Override
    public Span.Builder addLink(final Link link) {
        return this;
    }

    @Override
    public Span.Builder setSpanKind(final Span.Kind kind) {
        this.kind = kind;
        return this;
    }

    @Override
    public Span.Builder setStartTimestamp(final long startTimestamp) {
        this.startTimestamp = startTimestamp;
        return this;
    }

    @Override
    public Span startSpan() {
        if(startTimestamp == 0) {
            startTimestamp = System.nanoTime();
        }

        if(parentContext != null || parentSpan == null) {
            return EntitySpan.beginSegment(recorder, name, startTimestamp, kind);
        } else {
            return EntitySpan.beginSubsegment(recorder, name, (EntitySpan) parentSpan, startTimestamp, kind);
        }
    }
}
