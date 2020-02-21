package com.amazonaws.xray.opentelemetry.tracing.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.trace.AttributeValue;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EntityMetadataEventTest {

  EntityMetadataEvent e1;
  EntityMetadataEvent e2;

  @BeforeEach
  public void setup() {
    Map<String, AttributeValue> attrs =
        Collections.singletonMap("TEST", AttributeValue.booleanAttributeValue(true));
    e1 = EntityMetadataEvent.create("Test 1", attrs, 0L);
    e2 = EntityMetadataEvent.create("Test 2", 1L);
  }

  @Test
  public void when_EventsAreCompared_then_TheyAreOrderdByTimestamp() {
    assertEquals(-1, e1.compareTo(e2));
    assertEquals(0, e1.compareTo(e1));
    assertEquals(1, e2.compareTo(e1));
  }

  @Test
  public void when_EventsAreSerialized_then_jacksonDoesNotThrowExceptions()
      throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValueAsString(e1);
  }
}
