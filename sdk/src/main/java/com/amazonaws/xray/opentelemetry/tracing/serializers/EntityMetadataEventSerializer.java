package com.amazonaws.xray.opentelemetry.tracing.serializers;

import com.amazonaws.xray.opentelemetry.tracing.metadata.EntityMetadataEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.opentelemetry.trace.AttributeValue.Type;
import java.io.IOException;


public class EntityMetadataEventSerializer extends StdSerializer<EntityMetadataEvent> {


  public static final String NAME_FIELD = "name";
  public static final String ATTRIBUTES_FIELD = "attributes";
  public static final String TYPE_FIELD = "type";
  public static final String VALUE_FIELD = "value";

  public EntityMetadataEventSerializer() {
    this(null);
  }

  public EntityMetadataEventSerializer(final Class<EntityMetadataEvent> t) {
    super(t);
  }

  /**
   * Serialize an EntityMetadataEvent into JSON with correct handling of AttributeValues.
   * @param value the value to serialize
   * @param gen the JSON generator
   * @param provider the JSON provider
   * @throws IOException when an IO error occurs
   * @throws JsonProcessingException when a JSON processing error occurs
   */
  public void serialize(final EntityMetadataEvent value,
      final JsonGenerator gen,
      final SerializerProvider provider)
      throws IOException, JsonProcessingException {

    gen.writeStartObject();
    gen.writeObjectField(NAME_FIELD, value.getName());
    gen.writeObjectFieldStart(ATTRIBUTES_FIELD);
    value.getAttributes().forEach((s, attributeValue) -> {
      try {
        gen.writeObjectFieldStart(s);
        gen.writeStringField(TYPE_FIELD, attributeValue.getType().toString());

        /*
         * Jackson attempts to treat AttributeValue as a POJO
         * Calling the wrong-type of getValue() throws exceptions
         */
        if (attributeValue.getType() == Type.BOOLEAN) {
          gen.writeBooleanField(VALUE_FIELD, attributeValue.getBooleanValue());
        } else if (attributeValue.getType() == Type.LONG) {
          gen.writeNumberField(VALUE_FIELD, attributeValue.getLongValue());
        } else if (attributeValue.getType() == Type.DOUBLE) {
          gen.writeNumberField(VALUE_FIELD, attributeValue.getDoubleValue());
        } else if (attributeValue.getType() == Type.STRING) {
          gen.writeStringField(VALUE_FIELD, attributeValue.getStringValue());
        }

        gen.writeEndObject();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
    gen.writeEndObject();
    gen.writeEndObject();
  }
}
