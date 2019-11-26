package com.amazonaws.xray.opentelemetry.tracing;

import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.DummySegment;
import com.amazonaws.xray.entities.Entity;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.entities.TraceID;
import com.amazonaws.xray.opentelemetry.tracing.utils.ContextUtils;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.EndSpanOptions;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter between the OpenTelemetry Span API and X-Ray Entities.
 * @see io.opentelemetry.trace.Span
 * @param <T> the type of entity
 */
public class EntitySpan<T extends Entity> implements Span {

    private static final Log logger = LogFactory.getLog(EntitySpan.class);
    private static final String OT_METADATA_NAMESPACE = "open_telemetry";
    private static final String OT_METADATA_ATTRIBUTE_NAMESPACE = "attributes";
    private static final String OT_METADATA_KIND_NAMESPACE = "kind";
    private static final String OT_METADATA_STATUS_NAMESPACE = "status";

    private SpanContext context;
    private Map<String, Object> attributes;

    T entity;

    /**
     * Begin a span backed by a segment without updating the active Entity in the X-Ray recorder.
     * @param recorder create the span against this recorder
     * @param name the span's name
     * @param startTimestamp start time in nanoseconds
     * @param kind the OpenTelemetry span kind
     * @return the span
     */
    public static EntitySpan beginSegment(final AWSXRayRecorder recorder,
                                          final String name,
                                          final long startTimestamp,
                                          final Span.Kind kind) {

        Entity currentEntity = recorder.getTraceEntity();
        Segment newSegment = recorder.beginSegment(name);
        if(currentEntity == null) {
            recorder.clearTraceEntity();
        } else {
            recorder.setTraceEntity(currentEntity);
        }

        //TODO Implement nanosecond clock
        //newSegment.setStartTime(startTimestamp / 1000000.0d);

        return fromEntity(newSegment);
    }

    /**
     * Begin a span backed by a subsegment without updating the active Entity in the X-Ray recorder.
     * @param recorder create the span against this recorder
     * @param name the span's name
     * @param parent the parent span
     * @param startTimestamp start time in nanoseconds
     * @param kind the OpenTelemetry span kind
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

        if(currentEntity == null) {
            recorder.clearTraceEntity();
        } else {
            recorder.setTraceEntity(currentEntity);
        }

        return fromEntity(newSubsegment);
    }

    /**
     * Begin a span backed by a dummy segment.
     * @param recorder create the span against this recorder
     * @param name the span's name
     * @param startTimestamp start time in nanoseconds
     * @param kind the OpenTelemetry span kind
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
     * Begin a span backed by the provided entity.
     * @param entity the entity which backs the span
     * @return the span
     */
    public static EntitySpan fromEntity(final Entity entity) {
        Span.Kind kind = Kind.INTERNAL;
        Map<String, Map<String, Object>> metatdata = entity.getMetadata();

        if(metatdata != null) {
            Map<String, Object> otMetadata = entity.getMetadata().get(OT_METADATA_NAMESPACE);

            if(otMetadata != null) {
                Object spanKind = otMetadata.get(OT_METADATA_KIND_NAMESPACE);

                if(spanKind != null && spanKind instanceof Span.Kind) {
                    kind = (Span.Kind) spanKind;
                }
            }
        }

        return fromEntity(entity, kind);
    }

    public static EntitySpan fromEntity(final Entity entity, final Span.Kind kind) {
        return new EntitySpan<>(entity, kind);
    }

    private EntitySpan(final T entity, final Span.Kind kind) {
        this.entity = entity;

        entity.putMetadata(OT_METADATA_NAMESPACE, OT_METADATA_STATUS_NAMESPACE, Status.OK);
        entity.putMetadata(OT_METADATA_NAMESPACE, OT_METADATA_KIND_NAMESPACE, kind);

        Map<String,Map<String,Object>> metadata = entity.getMetadata();
        if(metadata != null) {
            Map<String, Object> otMetadata = metadata.get(OT_METADATA_NAMESPACE);
            if (otMetadata != null) {
                Map<String, Object> attributeMetadata =
                        (Map<String, Object>) otMetadata.get(OT_METADATA_ATTRIBUTE_NAMESPACE);
                if (attributeMetadata != null) {
                    attributes = attributeMetadata;
                }
            }
        }

        if(attributes == null) {
            attributes = new ConcurrentHashMap<>();
            entity.putMetadata(OT_METADATA_NAMESPACE, OT_METADATA_ATTRIBUTE_NAMESPACE, attributes);
        }
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
        if(AttributeValue.Type.BOOLEAN.equals(value.getType())) {
            setAttributeMetadata(key, value.getBooleanValue());
        }

        if(AttributeValue.Type.DOUBLE.equals(value.getType())) {
            setAttributeMetadata(key, value.getDoubleValue());
        }

        if(AttributeValue.Type.STRING.equals(value.getType())) {
            setAttributeMetadata(key, value.getStringValue());
        }

        if(AttributeValue.Type.LONG.equals(value.getType())) {
            setAttributeMetadata(key, value.getLongValue());
        }
    }

    private void setAttributeMetadata(final String key, final Object value) {
        switch(key) {
            case "http.method":
                if(value instanceof String) {
                    entity.putHttp("method", value);
                }
                break;
            case "http.status":
                if(value instanceof Long) {
                    entity.putHttp("status", value);
                }
                break;
            case "http.url":
                if(value instanceof String) {
                    entity.putHttp("url", value);
                }
                break;
            default:
                break;
        }

        attributes.put(key, value);
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
    public void addEvent(final String name, final Map<String, AttributeValue> attributes, final long timestamp) {

    }

    @Override
    public void addEvent(final Event event) {

    }

    @Override
    public void addEvent(final Event event, final long timestamp) {

    }

    @Override
    public void setStatus(final Status status) {
        entity.putMetadata(OT_METADATA_NAMESPACE, OT_METADATA_STATUS_NAMESPACE, status);
    }

    @Override
    public void updateName(final String name) {
        //Unsupported
    }

    @Override
    public void end() {
        if(isRecording()) {
            AWSXRayRecorder recorder = entity.getCreator();
            Entity previous = recorder.getTraceEntity();

            recorder.setTraceEntity(entity);

            if(entity instanceof Segment) {
                recorder.endSegment();
            } else if(entity instanceof Subsegment) {
                recorder.endSubsegment();
            }

            if(!entity.equals(previous)) {
                recorder.setTraceEntity(previous);
            }
        }
    }

    @Override
    public void end(final EndSpanOptions endOptions) {
        if(endOptions != null) {
            long endTime = endOptions.getEndTimestamp();
            //EndTime values are in nanoseconds
            entity.setEndTime(endTime / 1000000.0d);
        }
        end();
    }

    @Override
    public SpanContext getContext() {
        if(context == null) {
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
