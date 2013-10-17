package com.timpo.tapeworm.enums;

import com.timpo.tapeworm.other.Utils;

public enum FileTypes implements Identifiable {

  ENTIRE(192), //11000000 in binary
  BEGIN(128), //10000000 in binary
  BETWEEN(0), //00000000 in binary
  FINISH(64);   //01000000 in binary
  //
  private final byte id;

  private FileTypes(int id) {
    this.id = (byte) id;
  }

  public byte getID() {
    return id;
  }

  public static FileTypes find(byte id) {
    return Utils.get(id, FileTypes.values());
  }
}
