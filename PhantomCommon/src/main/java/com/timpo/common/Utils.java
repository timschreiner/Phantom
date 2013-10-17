package com.timpo.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

  private static final ObjectMapper JSON = new ObjectMapperFactory().build();
  private static String COMMON_PACKAGE_NAME = "com.timpo";

  public static Logger logFor(String className) {
    return LoggerFactory.getLogger(className);
  }

  public static String getLoggableException(Exception e) {
    for (StackTraceElement ste : e.getStackTrace()) {
      if (ste.toString().indexOf(COMMON_PACKAGE_NAME) != -1) {
        return e.getClass().getName() + " : " + e.getMessage() + " - " + ste.toString();
      }
    }

    return e.getClass().getName() + " : " + e.getMessage();
  }

  public static void ensure(Object o, String className) {
    if (o == null) {
      throw new IllegalArgumentException(className + "cannot be null");
    }
  }

  public static void ensure(boolean b, String errorMessage) {
    if (b == false) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  public static String generateUniqueID() {
    UUID id = UUID.randomUUID();

    ByteBuffer bb = ByteBuffer.allocate(16);

    bb.putLong(id.getMostSignificantBits());
    bb.putLong(id.getLeastSignificantBits());

    //base64 encoding should save a little bit of space with the id's
    return BaseEncoding.base64().encode(bb.array());
  }

  public static long currentTime() {
    return System.nanoTime();
  }

  public static TimeUnit defaultTimeUnit() {
    return TimeUnit.NANOSECONDS;
  }

  public static void sleep(int duration, TimeUnit interval) throws InterruptedException {
    Thread.sleep(TimeUnit.MILLISECONDS.convert(duration, interval));
  }

  public static Set<String> makeConcurrentSet() {
    return Collections.newSetFromMap(new ConcurrentHashMap());
  }

  public static String toJson(Object o) throws Exception {
    return JSON.writeValueAsString(o);
  }

  public static <T> T fromJson(String s, Class<T> klass) throws Exception {
    return JSON.readValue(s, klass);
  }

  public static Long fromNow(int i, TimeUnit tu) {
    return currentTime() + defaultTimeUnit().convert(i, tu);
  }
}
