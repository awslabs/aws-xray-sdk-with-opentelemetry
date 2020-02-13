package com.amazonaws.xray.opentelemetry.tracing.metadata;

import com.amazonaws.xray.entities.Entity;
import io.opentelemetry.trace.Span;
import java.util.Map;

public class EntityMetadataFactory {

  private static final String OT_METADATA_NAMESPACE = "sdk";
  private static final String OT_METADATA_KEY = "open_telemetry";

  /**
   * Return metadata from an Entity initializing it if it isn't already.
   * @param entity the entity
   * @param kind the kind to use if initializing new metadata
   * @return the metadata
   */
  public static EntityMetadata getOrCreate(final Entity entity, final Span.Kind kind) {

    Map<String, Object> otNamespace = entity.getMetadata().get(OT_METADATA_NAMESPACE);
    Object otMetadataObject = null;
    if (otNamespace != null) {
      otMetadataObject = otNamespace.get(OT_METADATA_KEY);
    }

    if (otMetadataObject instanceof EntityMetadata) {
      return (EntityMetadata) otMetadataObject;
    } else {
      EntityMetadata metadata = EntityMetadata.create(kind);
      entity.putMetadata(OT_METADATA_NAMESPACE, OT_METADATA_KEY, metadata);
      return metadata;
    }
  }
}
