package com.amazonaws.xray.opentelemetry.tracing;
import io.opentelemetry.trace.TracerFactory;
import io.opentelemetry.trace.spi.TracerFactoryProvider;

/**
 * SPI implementation for making this SDK available via OpenTelemetry.
 * @see io.opentelemetry.OpenTelemetry
 */
public class TracingProvider implements TracerFactoryProvider {

    @Override
    public TracerFactory create() {
        return RecorderBackedTracerFactory.create();
    }

}
