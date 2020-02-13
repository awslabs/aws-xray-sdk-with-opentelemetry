package com.amazonaws.xray.opentelemetry.tracing.metadata;

import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityMetadata {

  private final List<Event> events;
  private final Map<String, Object> attributes;
  private final Kind kind;
  private Status status;

  public static EntityMetadata create(final Kind kind) {
    return new EntityMetadata(kind);
  }

  private EntityMetadata(final Kind kind) {
    this.kind = kind;
    events = new ArrayList<>();
    attributes = new ConcurrentHashMap<>();
    status = Status.OK;
  }

  public void setStatus(final Status status) {
    this.status = status;
  }

  public void putAttribute(final String name, final Object value) {
    attributes.put(name, value);
  }

  public void addEvent(final Event event, final long timestamp) {
    events.add(event);
  }

  public Status getStatus() {
    return status;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public Kind getKind() {
    return kind;
  }

  public List<Event> getEvents() {
    return events;
  }
}
