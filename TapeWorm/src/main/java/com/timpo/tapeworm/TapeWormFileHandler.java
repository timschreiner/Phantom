package com.timpo.tapeworm;

import com.timpo.common.checksums.Checksums.ChecksumProvider.Checksummer;
import com.timpo.tapeworm.enums.Checksums;
import com.timpo.tapeworm.enums.FileTypes;
import java.io.InputStream;

public abstract class TapeWormFileHandler {

  public abstract void handle(InputStream fileInputStream, long fileLength, String filePath, FileTypes fileType, byte spanIndex, Checksums footerChecksum) throws Exception;

  protected void skipFile(InputStream fileInputStream, long fileLength, Checksummer footerChecksum) throws Exception {
    int checksumLength = footerChecksum.byteLength();
    long bytesToSkip = fileLength + checksumLength;

    fileInputStream.skip(bytesToSkip);
  }
}
