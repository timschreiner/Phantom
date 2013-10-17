package com.timpo.common.checksums;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Checksums {

  private static final ConcurrentMap<String, ChecksumProvider> CHECKSUMMERS = new ConcurrentHashMap<String, ChecksumProvider>();
  //initialize maps

  {
    CHECKSUMMERS.put("md5".toLowerCase(), new ChecksumProvider(MD5Checksum.class));
  }

  public static ChecksumProvider byName(String type) {
    ChecksumProvider csp = CHECKSUMMERS.get(type.toLowerCase());
    if (csp == null) {
      throw new IllegalArgumentException("cannot find checksum called '" + type + "'");
    } else {
      return csp;
    }
  }

  public static ChecksumProvider MD5() {
    return new ChecksumProvider(MD5Checksum.class);
  }

  public static class ChecksumProvider {

    private final Class<? extends ChecksumProvider.Checksummer> klass;

    public ChecksumProvider(Class<? extends ChecksumProvider.Checksummer> klass) {
      this.klass = klass;
    }

    public Checksummer makeChecksummer() {
      try {
        return klass.newInstance();

        //these really shouldn't be thrown
      } catch (InstantiationException ex) {
        Logger.getLogger(ChecksumProvider.class.getName()).log(Level.SEVERE, "problem instantiating checksum instance: " + klass.getName(), ex);
      } catch (IllegalAccessException ex) {
        Logger.getLogger(ChecksumProvider.class.getName()).log(Level.SEVERE, "problem instantiating checksum instance: " + klass.getName(), ex);
      }

      return null;
    }

    public static interface Checksummer {

      public void update(byte[] buffer, int lengthToRead);

      public byte[] checksum();

      public int byteLength();
    }
  }
}