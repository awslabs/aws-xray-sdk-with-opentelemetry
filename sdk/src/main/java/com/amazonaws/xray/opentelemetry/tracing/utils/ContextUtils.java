package com.amazonaws.xray.opentelemetry.tracing.utils;

import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.entities.TraceID;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;

public class ContextUtils {

    private static final String XRAY_VERSION_PREFIX = "1-";
    private static final String XRAY_SEPARATOR = "-";


    public static SpanContext entityToSpanContext(final Entity entity) {
        TraceId traceId = TraceId.fromLowerBase16(xrayTraceIdToOT(entity.getParentSegment().getTraceId().toString()), 0);
        SpanId spanId = SpanId.fromLowerBase16(entity.getId(), 0);
        TraceFlags flags = TraceFlags.builder()
                        .setIsSampled(entity.getParentSegment().isSampled())
                        .build();
        Tracestate state = Tracestate.getDefault();

        //Segments with a parent ID are usually remote
        if(entity instanceof Segment && entity.getParentId() != null) {
            return SpanContext.createFromRemoteParent(traceId, spanId, flags, state);
        } else {
            return SpanContext.create(traceId, spanId, flags, state);
        }
    }

    public static SpanContext headerToSpanContext(final TraceHeader header) {
        TraceId traceId = TraceId.fromLowerBase16(xrayTraceIdToOT(header.getRootTraceId().toString()), 0);
        SpanId spanId = SpanId.fromLowerBase16(header.getParentId().toLowerCase(), 0);
        TraceFlags flags = TraceFlags.builder()
                .setIsSampled(header.getSampled().equals(TraceHeader.SampleDecision.SAMPLED)
                        || header.getSampled().equals(TraceHeader.SampleDecision.REQUESTED))
                .build();
        Tracestate state = Tracestate.getDefault();
        return SpanContext.createFromRemoteParent(traceId, spanId, flags, state);
    }

    public static TraceHeader spanContextToHeader(final SpanContext context) {
        String otTraceId = context.getTraceId().toLowerBase16();
        String xrayTraceId = XRAY_VERSION_PREFIX + otTraceId.substring(0,8) + XRAY_SEPARATOR + otTraceId.substring(8);
        TraceID xrayId = TraceID.fromString(xrayTraceId);
        TraceHeader header = new TraceHeader();
        header.setSampled(context.getTraceFlags().isSampled() ?
                TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED);
        header.setRootTraceId(xrayId);
        header.setParentId(context.getSpanId().toLowerBase16());

        return header;
    }

    private static String xrayTraceIdToOT(String xrayId) {
        String otId = xrayId.substring(2).replaceAll("-", "");
        return otId;
    }
}
