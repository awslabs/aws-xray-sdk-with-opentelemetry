package com.amazonaws.xray.opentelemetry.tracing;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracerFactory;

/**
 * A factory for creating RecorderBackedTracers.
 * @see io.opentelemetry.trace.Tracer
 */
public class RecorderBackedTracerFactory implements TracerFactory {

    private final AWSXRayRecorder recorder;
    private RecorderBackedTracer tracer = null;

    /**
     * Create a new tracer factory.
     * @return the factory
     */
    public static final RecorderBackedTracerFactory create() {
       return new RecorderBackedTracerFactory(AWSXRay.getGlobalRecorder());
    }

    private RecorderBackedTracerFactory(AWSXRayRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public Tracer get(final String instrumentationName) {
        return get(instrumentationName, null);
    }

    @Override
    public Tracer get(final String instrumentationName, final String instrumentationVersion) {
        if(tracer == null) {
            synchronized(this) {
                if(tracer == null) {
                    tracer = RecorderBackedTracer.fromRecorder(recorder);
                }
            }
        }

        return tracer;
    }
}
