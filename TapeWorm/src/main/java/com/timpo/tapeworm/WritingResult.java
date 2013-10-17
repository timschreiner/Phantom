package com.timpo.tapeworm;

public class WritingResult {

  private final long fileWritten;
  private final long totalWritten;

  static WritingResult nothingWritten() {
    return new WritingResult(0, 0);
  }

  //<editor-fold defaultstate="collapsed" desc="generated-code">
  public WritingResult(long fileWritten, long totalWritten) {
    if (fileWritten > totalWritten) {
      throw new IllegalArgumentException("you cannot write more to the file than you wrote to the header+file+footer");
    }

    this.fileWritten = fileWritten;
    this.totalWritten = totalWritten;
  }

  public long getFileWritten() {
    return fileWritten;
  }

  public long getTotalWritten() {
    return totalWritten;
  }

  @Override
  public String toString() {
    return "WritingResult{" + "fileWritten=" + fileWritten + ", totalWritten=" + totalWritten + '}';
  }
  //</editor-fold>
}
