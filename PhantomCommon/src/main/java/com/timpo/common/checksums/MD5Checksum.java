package com.timpo.common.checksums;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.timpo.common.checksums.Checksums.ChecksumProvider.Checksummer;

final public class MD5Checksum implements Checksummer {

  private final Hasher hf;

  public MD5Checksum() {
    hf = Hashing.md5().newHasher();
  }

  @Override
  public void update(byte[] buffer, int lengthToRead) {
    hf.putBytes(buffer, 0, lengthToRead);
  }

  @Override
  public byte[] checksum() {
    return hf.hash().asBytes();
  }

  @Override
  public int byteLength() {
    return 16;
  }
}
