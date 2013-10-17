package com.timpo.tapeworm;

import com.timpo.common.checksums.Checksums.ChecksumProvider.Checksummer;
import com.timpo.tapeworm.markers.SequenceMarker;
import com.timpo.tapeworm.markers.FileMarker;
import com.timpo.tapeworm.markers.PathMarker;
import com.timpo.tapeworm.enums.StringEncodings;
import com.timpo.tapeworm.enums.Checksums;
import com.timpo.tapeworm.enums.FileTypes;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TapeWormWriter {

  private static final int DEFAULT_BUFFER_SIZE = 32768; //32kb
  //TODO: what should this value be?
  private static final long MINIMUM_BYTES_WORTH_CREATING_FILE_FOR = 1024; //1 kb

  public static WritingResult writeTapeWormFile(OutputStream toWriteTo, InputStream toReadFrom,
          String filePath, StringEncodings pathEncoding, byte spanIndex,
          long fileSize, long bytesAlreadyWritten, long outputBytesRemaining,
          Checksums checksum) throws IOException {

    //encode the path
    byte[] encodedPath = filePath.getBytes(pathEncoding.getCharset());
    PathMarker pathMarker = new PathMarker(encodedPath, pathEncoding);

    //determine how much space we need for the header
    int headerLength = SequenceMarker.byteLength() + pathMarker.byteLength() + FileMarker.byteLength();

    //determine how much space we need for the footer
    int footerLength = checksum.byteLength();

    //determine how much of the file can be written given the header, footer and
    //remaining space constraints
    long outputBytesRemainingAfterWritingHeaderAndFooter = outputBytesRemaining - headerLength - footerLength;
    if (outputBytesRemainingAfterWritingHeaderAndFooter < MINIMUM_BYTES_WORTH_CREATING_FILE_FOR) {
      //it's not worth writing anything this small
      return WritingResult.nothingWritten();
    }

    long bytesNeedingToBeWritten = fileSize - bytesAlreadyWritten;
    long bytesToWrite = Math.min(bytesNeedingToBeWritten, outputBytesRemainingAfterWritingHeaderAndFooter);

    SequenceMarker sequenceMarker = encodeSequenceMarker(spanIndex, bytesNeedingToBeWritten, bytesToWrite, checksum);

    FileMarker fileMarker = new FileMarker(bytesToWrite);

    //write out the file
    writeOut(toWriteTo, sequenceMarker, pathMarker, fileMarker,
            encodedPath, toReadFrom, bytesToWrite, checksum);

    //let the caller know how much we were able to write (of the file and to the stream)
    return new WritingResult(bytesToWrite, headerLength + bytesToWrite + footerLength);
  }

  private static SequenceMarker encodeSequenceMarker(byte spanIndex, long bytesNeedingToBeWritten, long bytesToWrite, Checksums checksum) {
    boolean readingFromTheStart = spanIndex == 0;
    boolean writingToTheEnd = bytesNeedingToBeWritten == bytesToWrite;

    FileTypes fileType;
    if (readingFromTheStart && writingToTheEnd) {
      fileType = FileTypes.ENTIRE;

    } else if (readingFromTheStart && !writingToTheEnd) {
      fileType = FileTypes.BEGIN;

    } else if (!readingFromTheStart && writingToTheEnd) {
      fileType = FileTypes.FINISH;

    } else {
      fileType = FileTypes.BETWEEN;
    }

    return new SequenceMarker(fileType, spanIndex, checksum);
  }

  private static void writeOut(OutputStream toWriteTo, SequenceMarker sequenceMarker,
          PathMarker pathMarker, FileMarker fileMarker, byte[] encodedPath,
          InputStream toReadFrom, long bytesToWrite, Checksums checksumToAppend) throws IOException {

    DataOutputStream os = new DataOutputStream(toWriteTo);
    os.writeInt(sequenceMarker.makeIntMarker());
    os.writeShort(pathMarker.makeShortMarker());
    os.writeLong(fileMarker.makeLongMarker());
    os.write(encodedPath);

    writeFileAndChecksum(os, toReadFrom, bytesToWrite, checksumToAppend);
  }

  private static void writeFileAndChecksum(DataOutputStream os,
          InputStream is, long bytesRemainingToWrite, Checksums checksumToAppend) throws IOException {

    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

    Checksummer cs = checksumToAppend.makeChecksummer();

    int amountToRead = buffer.length;

    //read through the input stream
    while (true) {
      if (bytesRemainingToWrite <= 0) {
        break;
      }

      //if completely filling the buffer would read past what we want to write,
      //only fill it as much as you need
      if (bytesRemainingToWrite - buffer.length < 0) {
        amountToRead = (int) bytesRemainingToWrite;
      }

      //read into the buffer
      int numRead = is.read(buffer, 0, amountToRead);

      //stop if we've reached the end of the file
      if (numRead == -1) {
        break;
      }

      //track how much we've got left to write
      bytesRemainingToWrite -= numRead;

      //run what we've read through the checksum
      cs.update(buffer, numRead);

      //write out what we've read
      os.write(buffer, 0, numRead);
    }

    //get the hex version of the digest and write it after the file is finished
    os.write(cs.checksum());
  }
}
