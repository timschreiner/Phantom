package com.timpo.tapeworm;

import com.timpo.tapeworm.enums.Checksums;
import com.timpo.tapeworm.enums.FileTypes;
import com.timpo.tapeworm.enums.StringEncodings;
import com.timpo.tapeworm.markers.FileMarker;
import com.timpo.tapeworm.markers.PathMarker;
import com.timpo.tapeworm.markers.SequenceMarker;
import java.io.DataInputStream;
import java.io.InputStream;

public class TapeWormReader {

  public static void readTapeWormFile(InputStream toReadFrom, TapeWormFileHandler pfh) throws Exception {
    DataInputStream is = new DataInputStream(toReadFrom);

    //the first int is the sequence marker
    SequenceMarker sm = SequenceMarker.parse(is.readInt());
    FileTypes fileType = sm.getFileType();
    byte spanIndex = sm.getSpanIndex();
    Checksums checksum = sm.getChecksum();

    //the next short is the path marker
    PathMarker pm = PathMarker.parse(is.readShort());
    short pathLength = pm.getPathLength();
    StringEncodings pathEncoding = pm.getPathEncoding();

    //the next long is the file marker
    FileMarker fm = FileMarker.parse(is.readLong());
    long fileLength = fm.getFileLength();

    //the next pathBytes-bytes are the encoded filePath
    byte[] pathBytes = new byte[pathLength];
    is.readFully(pathBytes);
    String filePath = new String(pathBytes, pathEncoding.getCharset());

    //the next fileLength-bytes are the file
    pfh.handle(toReadFrom, fileLength, filePath, fileType, spanIndex, checksum);
  }

  private static FileTypes parseFileType(byte sequenceMarker) {
    byte id = (byte) (sequenceMarker & 192); //11000000 in binary
    return FileTypes.find(id);
  }

  private static byte parseSpanIndex(byte sequenceMarker) {
    return (byte) (sequenceMarker & 63); //00111111 in binary
  }
}
