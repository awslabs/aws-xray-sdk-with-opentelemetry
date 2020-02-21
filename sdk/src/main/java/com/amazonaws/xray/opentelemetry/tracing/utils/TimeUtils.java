package com.amazonaws.xray.opentelemetry.tracing.utils;

import java.time.Instant;

public class TimeUtils {

  private static final Long MS_TO_NS = 1000000L;
  private static final Double NS_TO_S = 1e+9D;

  /**
   * Return the current epoch time as a nanosecond Long.
   * @return the current time in epoch nanoseconds
   */
  public static Long getCurrentNanoTime() {
    Instant now = Instant.now();
    return (now.toEpochMilli() * MS_TO_NS) + now.getNano();
  }

  /**
   * Convert a long containing epoch nanoseconds into a double containing seconds.
   * @param nanoTime the nanosecond epoch time
   * @return a double precision epoch second timestamp
   */
  public static Double nanoTimeToXrayTimestamp(Long nanoTime) {
    return nanoTime / NS_TO_S;
  }

  /**
   * Return the current time with nanosecond resolution as a double containing epoch seconds.
   * @return the double precision epoch second timestamp with nanosecond resolution
   */
  public static Double currentXrayNanoTimestamp() {
    return nanoTimeToXrayTimestamp(getCurrentNanoTime());
  }

}
