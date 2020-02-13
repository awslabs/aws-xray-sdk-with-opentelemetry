package com.amazonaws.xray.opentelemetry.tracing.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.entities.TraceID;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ContextUtilsTest {


  private static final String X_RAY_TRACE_HEADER
      = "Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1";
  private static final String X_RAY_TRACE_ID = "1-5759e988-bd862e3fe1be46a994272793";
  private static final String OT_TRACE_ID = "5759e988bd862e3fe1be46a994272793";
  private static final String SEGMENT_ID = "53995c3f42cd8ad8";

  private TraceId otTraceId;
  private TraceID xrayTraceId;
  private SpanId spanId;
  private TraceFlags flags;
  private Tracestate state = Tracestate.getDefault();
  private TraceHeader header;
  private SpanContext context;

  @Mock
  private Segment segment;


  @BeforeEach
  public void setup() {
    xrayTraceId = TraceID.fromString(X_RAY_TRACE_ID);
    otTraceId = TraceId.fromLowerBase16(OT_TRACE_ID, 0);
    spanId = SpanId.fromLowerBase16(SEGMENT_ID, 0);
    flags = TraceFlags.builder().setIsSampled(true).build();
    context = SpanContext.createFromRemoteParent(otTraceId, spanId, flags, state);
    header = TraceHeader.fromString(X_RAY_TRACE_HEADER);
  }


  @Test
  public void when_aConTextIsGiven_then_aHeaderIsReturned() {
    assertEquals(context, ContextUtils.headerToSpanContext(header));
  }

  @Test
  public void when_aHeaderIsGiven_then_aContextIsReturned() {
    assertEquals(X_RAY_TRACE_HEADER, ContextUtils.spanContextToHeader(context).toString());
  }

  @Test
  public void when_anEntityIsGiven_then_ContextIsReturned() {
    when(segment.getParentSegment()).thenReturn(segment);
    when(segment.getTraceId()).thenReturn(xrayTraceId);
    when(segment.getId()).thenReturn(SEGMENT_ID);
    when(segment.isSampled()).thenReturn(true);
    when(segment.getParentId()).thenReturn(SEGMENT_ID);
    assertEquals(context, ContextUtils.entityToSpanContext(segment));
  }
}
