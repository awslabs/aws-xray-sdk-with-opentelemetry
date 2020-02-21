package com.amazonaws.xray.opentelemetry.tracing.metadata;

import com.amazonaws.xray.opentelemetry.tracing.serializers.EntityMetadataEventSerializer;
import com.amazonaws.xray.opentelemetry.tracing.utils.TimeUtils;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import java.util.Collections;
import java.util.Map;

@JsonSerialize(using = EntityMetadataEventSerializer.class)
public class EntityMetadataEvent implements Event, Comparable<EntityMetadataEvent> {
  private String name;
  private Map<String, AttributeValue> attributes;
  private long timestamp;

  public static EntityMetadataEvent create(final String name) {
    return new EntityMetadataEvent(name,
        Collections.unmodifiableMap(Collections.emptyMap()), TimeUtils.getCurrentNanoTime());
  }

  public static EntityMetadataEvent create(final String name,
      final Map<String, AttributeValue> attributes) {
    return new EntityMetadataEvent(name, attributes, TimeUtils.getCurrentNanoTime());
  }

  public static EntityMetadataEvent create(final String name, final long timestamp) {
    return new EntityMetadataEvent(name,
        Collections.unmodifiableMap(Collections.emptyMap()), timestamp);
  }

  public static EntityMetadataEvent create(final String name,
      final Map<String, AttributeValue> attributes, final long timestamp) {
    return new EntityMetadataEvent(name, attributes, timestamp);
  }

  public static EntityMetadataEvent create(final Event event) {
    return new EntityMetadataEvent(event.getName(), event.getAttributes(),
        TimeUtils.getCurrentNanoTime());
  }

  public static EntityMetadataEvent create(final Event event, final long timestamp) {
    return new EntityMetadataEvent(event.getName(), event.getAttributes(), timestamp);
  }

  private EntityMetadataEvent(final String name, final Map<String, AttributeValue> attributes,
      final long timestamp) {
    this.attributes = attributes;
    this.name = name;
    this.timestamp = timestamp;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Map<String, AttributeValue> getAttributes() {
    return attributes;
  }

  @Override
  public int compareTo(EntityMetadataEvent o) {
    return Long.compare(timestamp, o.timestamp);
  }
}
