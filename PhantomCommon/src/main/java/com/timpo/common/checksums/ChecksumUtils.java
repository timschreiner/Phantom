package com.timpo.common.checksums;

import com.timpo.common.checksums.Checksums.ChecksumProvider;
import com.timpo.common.checksums.Checksums.ChecksumProvider.Checksummer;
import java.io.InputStream;
import org.apache.commons.codec.binary.Hex;

public class ChecksumUtils {
  //TODO: determine how big this buffer should be

  private static final int DEFAULT_BUFFER_SIZE = 32768; //32kb

  public static String hexChecksum(InputStream is, ChecksumProvider csp) throws Exception {
    return Hex.encodeHexString(bytesChecksum(is, csp));
  }

  public static byte[] bytesChecksum(InputStream is, ChecksumProvider csp) throws Exception {
    return bytesChecksum(is, csp, DEFAULT_BUFFER_SIZE);
  }

  public static byte[] bytesChecksum(InputStream is, ChecksumProvider csp, int buffersize) throws Exception {
    Checksummer cs = csp.makeChecksummer();

    byte[] buffer = new byte[buffersize];

    //read through the input stream
    while (true) {
      int numRead = is.read(buffer);

      if (numRead == -1) {
        break;
      }

      cs.update(buffer, numRead);
    }

    //get the hex version of the digest
    return cs.checksum();
  }
}
