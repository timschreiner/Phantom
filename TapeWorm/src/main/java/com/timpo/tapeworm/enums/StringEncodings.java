package com.timpo.tapeworm.enums;

import com.timpo.tapeworm.other.Utils;
import com.google.common.base.Charsets;
import java.nio.charset.Charset;

public enum StringEncodings implements Identifiable {

  UTF_16BE(1, Charsets.UTF_16BE);
  private final byte id;
  private final Charset charset;

  StringEncodings(int id, Charset charset) {
    this.id = (byte) id;
    this.charset = charset;
  }

  public byte getID() {
    return id;
  }

  public Charset getCharset() {
    return charset;
  }

  public static StringEncodings find(byte id) {
    return Utils.get(id, StringEncodings.values());
  }
}
