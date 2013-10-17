package com.timpo.tapeworm.enums;

import com.timpo.common.checksums.Checksums.ChecksumProvider;
import com.timpo.common.checksums.Checksums.ChecksumProvider.Checksummer;
import com.timpo.common.checksums.MD5Checksum;
import com.timpo.tapeworm.other.Utils;

public enum Checksums implements Identifiable {

  MD5(1, MD5Checksum.class);
  private final byte id;
  private final int length;
  private final ChecksumProvider csp;

  Checksums(int id, Class<? extends Checksummer> klass) {
    this.id = (byte) id;
    csp = new ChecksumProvider(klass);
    length = csp.makeChecksummer().byteLength();
  }

  public byte getID() {
    return id;
  }

  public Checksummer makeChecksummer() {
    return csp.makeChecksummer();
  }

  public int byteLength() {
    return length;
  }

  public static Checksums get(byte id) {
    return Utils.get(id, Checksums.values());
  }
}
