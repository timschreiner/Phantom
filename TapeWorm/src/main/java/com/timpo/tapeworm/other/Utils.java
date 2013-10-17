package com.timpo.tapeworm.other;

import com.timpo.tapeworm.enums.Identifiable;

public class Utils {

  public static <T extends Identifiable> T get(byte id, T[] values) {
    for (T t : values) {
      if (t.getID() == id) {
        return t;
      }
    }

    throw new IllegalArgumentException("cannot find instance for id=" + id);
  }

  public static long twoToThe(int i) {
    return (long) Math.pow(2, i);
  }
}
