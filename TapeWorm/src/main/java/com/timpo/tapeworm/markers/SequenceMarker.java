package com.timpo.tapeworm.markers;

import com.timpo.tapeworm.enums.Checksums;
import com.timpo.tapeworm.enums.FileTypes;

public class SequenceMarker {

  private static final int MAX_SPAN_INDEX = 256 - 1;     //a byte for the order of this file in a span in SEQUENCE_MARKER
  //
  private final FileTypes fileType;
  private final byte spanIndex;
  private final Checksums checksum;

  public SequenceMarker(FileTypes fileType, byte spanIndex, Checksums checksum) {
    if (spanIndex > MAX_SPAN_INDEX) {
      throw new IllegalArgumentException("cannot encode span index greater than " + MAX_SPAN_INDEX);
    }

    this.fileType = fileType;
    this.spanIndex = spanIndex;
    this.checksum = checksum;
  }

  public int makeIntMarker() {
    int fileTypeMask = fileType.getID() << 24;
    int spanIndexMask = spanIndex << 16;
    int checksumMask = checksum.getID();

    //the fileType is the first byte, the span index is the following short, the checksum is the last byte
    return fileTypeMask | spanIndexMask | checksumMask;
  }

  public static SequenceMarker parse(int sm) {
    //the checksumID was in the bottom byte, so we need to mask off anything above that
    byte checksumID = (byte) (sm & 255); //00000000000000000000000011111111 in binary

    //the spanIndex was in the middle short, so we need to mask off anything above that
    byte spanIndex = (byte) ((sm >> 16) & 65535); //00000000000000001111111111111111 in binary

    //the fileType was in the top byte, so there's nothing to mask off
    byte fileTypeID = (byte) (sm >> 24);

    return new SequenceMarker(FileTypes.find(fileTypeID), spanIndex, Checksums.get(checksumID));
  }

  public FileTypes getFileType() {
    return fileType;
  }

  public Checksums getChecksum() {
    return checksum;
  }

  public byte getSpanIndex() {
    return spanIndex;
  }

  public static int byteLength() {
    //the fileType is the first byte, the span index is the following short, the checksum is the last byte
    return 4;
  }
}
