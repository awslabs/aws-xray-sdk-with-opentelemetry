package com.amazonaws.xray.opentelemetry.tracing;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.DummySegment;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.entities.TraceID;
import com.amazonaws.xray.opentelemetry.tracing.metadata.EntityMetadata;
import com.amazonaws.xray.opentelemetry.tracing.metadata.EntityMetadataFactory;
import com.amazonaws.xray.opentelemetry.tracing.utils.ContextUtils;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.EndSpanOptions;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Status;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Adapter between the OpenTelemetry Span API and X-Ray Entities.
 *
 * @param <T> the type of entity
 * @see io.opentelemetry.trace.Span
 */
public class EntitySpan<T extends Entity> implements Span {

  private static final Log logger = LogFactory.getLog(EntitySpan.class);
  private final T entity;
  private final EntityMetadata metadata;
  private SpanContext context;

  private EntitySpan(final T entity, final Span.Kind kind) {
    this.entity = entity;
    this.metadata = EntityMetadataFactory.getOrCreate(entity, kind);
  }

  /**
   * Begin a span backed by a segment without updating the active Entity in the X-Ray recorder.
   *
   * @param recorder       create the span against this recorder
   * @param name           the span's name
   * @param startTimestamp start time in nanoseconds
   * @param kind           the OpenTelemetry span kind
   * @return the span
   */
  public static EntitySpan beginSegment(final AWSXRayRecorder recorder,
      final String name,
      final long startTimestamp,
      final Span.Kind kind) {

    Entity currentEntity = recorder.getTraceEntity();
    Segment newSegment = recorder.beginSegment(name);
    if (currentEntity == null) {
      recorder.clearTraceEntity();
    } else {
      recorder.setTraceEntity(currentEntity);
    }

    //TODO Implement nanosecond clock
    //newSegment.setStartTime(startTimestamp / 1000000.0d);

    return fromEntity(newSegment, kind);
  }

  /**
   * Begin a span backed by a subsegment without updating the active Entity in the X-Ray recorder.
   *
   * @param recorder       create the span against this recorder
   * @param name           the span's name
   * @param parent         the parent span
   * @param startTimestamp start time in nanoseconds
   * @param kind           the OpenTelemetry span kind
   * @return the span
   */
  public static EntitySpan beginSubsegment(final AWSXRayRecorder recorder,
      final String name,
      final EntitySpan parent,
      final long startTimestamp,
      final Span.Kind kind) {
    Entity currentEntity = recorder.getTraceEntity();
    recorder.setTraceEntity(parent.getXrayEntity());

    Subsegment newSubsegment = recorder.beginSubsegment(name);

    //TODO Implement nanosecond clock
    //newSubsegment.setStartTime(startTimestamp / 1000000.0d);

    if (currentEntity == null) {
      recorder.clearTraceEntity();
    } else {
      recorder.setTraceEntity(currentEntity);
    }

    return fromEntity(newSubsegment, kind);
  }

  /**
   * Begin a span backed by a dummy segment.
   *
   * @param recorder       create the span against this recorder
   * @param name           the span's name
   * @param startTimestamp start time in nanoseconds
   * @param kind           the OpenTelemetry span kind
   * @return the span
   */
  public static EntitySpan beginDummySegment(final AWSXRayRecorder recorder,
      final String name,
      final long startTimestamp,
      final Span.Kind kind) {
    DummySegment dummySegment = new DummySegment(recorder, new TraceID());

    //TODO Implement nanosecond clock
    //dummySegment.setStartTime(startTimestamp / 1000000.0d);

    return fromEntity(dummySegment, kind);
  }

  /**
   * Create a span from the provided entity with the default INTERNAL kind.
   *
   * @param entity the entity which backs the span
   * @return the span
   */
  public static EntitySpan fromEntity(final Entity entity) {
    return fromEntity(entity, Kind.INTERNAL);
  }

  /**
   * Create a span from the provided entity with the specified kind.
   *
   * @param entity the entity which backs the span
   * @param kind the kind of span
   * @return the span
   */
  public static EntitySpan fromEntity(final Entity entity, final Span.Kind kind) {
    return new EntitySpan<>(entity, kind);
  }

  @Override
  public void setAttribute(final String key, final String value) {
    setAttributeMetadata(key, value);
  }

  @Override
  public void setAttribute(final String key, final long value) {
    setAttributeMetadata(key, value);
  }

  @Override
  public void setAttribute(final String key, final double value) {
    setAttributeMetadata(key, value);
  }

  @Override
  public void setAttribute(final String key, final boolean value) {
    setAttributeMetadata(key, value);
  }

  @Override
  public void setAttribute(final String key, final AttributeValue value) {
    if (AttributeValue.Type.BOOLEAN.equals(value.getType())) {
      setAttributeMetadata(key, value.getBooleanValue());
    }

    if (AttributeValue.Type.DOUBLE.equals(value.getType())) {
      setAttributeMetadata(key, value.getDoubleValue());
    }

    if (AttributeValue.Type.STRING.equals(value.getType())) {
      setAttributeMetadata(key, value.getStringValue());
    }

    if (AttributeValue.Type.LONG.equals(value.getType())) {
      setAttributeMetadata(key, value.getLongValue());
    }
  }

  private void setAttributeMetadata(final String key, final Object value) {
    switch (key) {
      case "http.method":
        if (value instanceof String) {
          putHttpAttribute("request","method", value);
        }
        break;
      case "http.status_code":
        if (value instanceof Long) {
          putHttpAttribute("response","status", value);
        }
        break;
      case "http.url":
        if (value instanceof String) {
          putHttpAttribute("request","url", value);
        }
        break;
      default:
        break;
    }

    metadata.putAttribute(key, value);
  }

  private void putHttpAttribute(final String section, final String key, final Object value) {
    Map<String,Object> http = entity.getHttp();
    Map<String, Object> sectionMap;

    if(http.containsKey(section) && http.get(section) instanceof Map) {
      sectionMap = (Map<String, Object>) http.get(section);
    } else {
      sectionMap = new HashMap<>();
      entity.putHttp(section, sectionMap);
    }

    sectionMap.put(key, value);
  }

  //TODO The following event and status methods just update properties in the Segment
  @Override
  public void addEvent(final String name) {

  }

  @Override
  public void addEvent(final String name, final long timestamp) {

  }

  @Override
  public void addEvent(final String name, final Map<String, AttributeValue> attributes) {

  }

  @Override
  public void addEvent(final String name, final Map<String, AttributeValue> attributes,
      final long timestamp) {

  }

  @Override
  public void addEvent(final Event event) {

  }

  @Override
  public void addEvent(final Event event, final long timestamp) {

  }

  @Override
  public void setStatus(final Status status) {
    metadata.setStatus(status);
  }

  @Override
  public void updateName(final String name) {
    //Unsupported
  }

  @Override
  public void end() {
    if (isRecording()) {
      AWSXRayRecorder recorder = entity.getCreator();
      Entity previous = recorder.getTraceEntity();

      recorder.setTraceEntity(entity);

      if (entity instanceof Segment) {
        recorder.endSegment();
      } else if (entity instanceof Subsegment) {
        recorder.endSubsegment();
      }

      if (!entity.equals(previous)) {
        recorder.setTraceEntity(previous);
      }
    }
  }

  @Override
  public void end(final EndSpanOptions endOptions) {
    if (endOptions != null) {
      long endTime = endOptions.getEndTimestamp();
      //EndTime values are in nanoseconds
      entity.setEndTime(endTime / 1000000.0d);
    }
    end();
  }

  @Override
  public SpanContext getContext() {
    if (context == null) {
      context = ContextUtils.entityToSpanContext(entity);
    }
    return context;
  }

  @Override
  public boolean isRecording() {
    return entity.isInProgress();
  }

  public Entity getXrayEntity() {
    return entity;
  }
}
