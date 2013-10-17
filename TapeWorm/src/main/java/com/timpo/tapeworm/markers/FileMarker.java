package com.timpo.tapeworm.markers;

public class FileMarker {

  private long fileLength;

  public FileMarker(long fileLength) {
    this.fileLength = fileLength;
  }

  public static FileMarker parse(long fm) {
    return new FileMarker(fm);
  }

  public long getFileLength() {
    return fileLength;
  }

  public long makeLongMarker() {
    //for now, no complex masking is involved
    return fileLength;
  }

  public static int byteLength() {
    //just a long representing how many bytes long the file is
    return 8;
  }
}
