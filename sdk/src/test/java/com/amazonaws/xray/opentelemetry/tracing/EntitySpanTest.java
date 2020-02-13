package com.amazonaws.xray.opentelemetry.tracing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import io.opentelemetry.trace.Span;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EntitySpanTest {

  @Mock
  AWSXRayRecorder recorder;

  @Mock
  Segment segment;

  @Mock
  Subsegment subsegment;

  @Mock
  Segment previousSegment;

  @Test
  public void when_aSegmentIsCreatedAndEnded_then_theCurrentEntityIsPreserved() {
    when(recorder.beginSegment(anyString())).thenReturn(segment);
    when(recorder.getTraceEntity()).thenReturn(previousSegment);
    when(segment.getCreator()).thenReturn(recorder);
    when(segment.isInProgress()).thenReturn(true);

    Span testSpan = EntitySpan.beginSegment(recorder, "Test Span", 0, Span.Kind.INTERNAL);
    testSpan.end();

    //Verify Order of Operations
    InOrder inOrder = inOrder(recorder);
    //Create new span
    inOrder.verify(recorder).getTraceEntity();
    inOrder.verify(recorder).beginSegment(anyString());
    inOrder.verify(recorder).setTraceEntity(previousSegment);

    //End span
    inOrder.verify(recorder).getTraceEntity();
    inOrder.verify(recorder).setTraceEntity(segment);
    inOrder.verify(recorder).endSegment();
    inOrder.verify(recorder).setTraceEntity(previousSegment);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void when_aSegmentExists_then_aSubsegemtnIsCreated() {
    EntitySpan parentSpan = mock(EntitySpan.class);
    when(parentSpan.getXrayEntity()).thenReturn(segment);
    when(subsegment.getCreator()).thenReturn(recorder);
    when(recorder.getTraceEntity()).thenReturn(previousSegment);
    when(recorder.beginSubsegment(anyString())).thenReturn(subsegment);
    when(subsegment.isInProgress()).thenReturn(true);

    Span subsegmentSpan = EntitySpan
        .beginSubsegment(recorder, "Test Child", parentSpan, 0, Span.Kind.INTERNAL);
    subsegmentSpan.end();

    //Verify Order of Operations
    InOrder inOrder = inOrder(recorder);
    //Create new subsegment
    inOrder.verify(recorder).getTraceEntity();
    inOrder.verify(recorder).setTraceEntity(segment);
    inOrder.verify(recorder).beginSubsegment(anyString());
    inOrder.verify(recorder).setTraceEntity(previousSegment);

    //End span
    inOrder.verify(recorder).getTraceEntity();
    inOrder.verify(recorder).setTraceEntity(subsegment);
    inOrder.verify(recorder).endSubsegment();
    inOrder.verify(recorder).setTraceEntity(previousSegment);
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void when_aSegmentHasHttpParams_then_segmentHasHttpBlock() {
    when(recorder.beginSegment(anyString())).thenReturn(segment);
    Span testSpan = EntitySpan.beginSegment(recorder, "Test Span", 0, Span.Kind.INTERNAL);
    testSpan.setAttribute("http.method", "GET");
    testSpan.setAttribute("http.status_code", 200L);
    testSpan.setAttribute("http.url", "http://test.com");
    testSpan.end();

    verify(segment, times(2)).putHttp(eq("request"), any(Map.class));
    verify(segment).putHttp(eq("response"), any(Map.class));
  }

  @Test
  public void when_isRecordingCalled_then_isInProgressCalled() {
    when(recorder.beginSegment(anyString())).thenReturn(segment);
    when(segment.isInProgress()).thenReturn(false);
    Span testSpan = EntitySpan.beginSegment(recorder, "Test Span", 0, Span.Kind.INTERNAL);
    assertEquals(false, testSpan.isRecording());
  }


}
