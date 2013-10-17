package com.timpo.tapeworm.markers;

import com.timpo.tapeworm.enums.StringEncodings;

public class PathMarker {

  private static final int MAX_ENCODING_ID = 8 - 1;     //high 3 bits of a short for the encoding id in PATH_MARKER
  private static final int MAX_PATH_LENGTH = 8192 - 1;  //low 13 bits of a short for the path length in PATH_MARKER
  //
  private final StringEncodings pathEncoding;
  private final short pathLength;

  public PathMarker(byte[] encodedPath, StringEncodings pathEncoding) {
    this.pathEncoding = pathEncoding;
    pathLength = (short) encodedPath.length;
  }

  public PathMarker(short pathLength, StringEncodings pathEncoding) {
    this.pathEncoding = pathEncoding;
    this.pathLength = pathLength;
  }

  public short makeShortMarker() {
    byte encodingID = pathEncoding.getID();

    if (encodingID > MAX_ENCODING_ID) {
      throw new IllegalArgumentException("cannot encode string encoding IDs greater than " + MAX_ENCODING_ID);
    } else if (pathLength > MAX_PATH_LENGTH) {
      throw new IllegalArgumentException("cannot encode paths longer than " + MAX_PATH_LENGTH);
    }

    //the highest 3 bits are the encodingID
    int encodingIDMask = encodingID << 13;

    //the remaining 13 are the path length
    int pathLengthMask = pathLength;

    return (short) (encodingIDMask | pathLengthMask);
  }

  public static PathMarker parse(short pm) {
    //the encodingID was in the top 3 bits
    byte encodingID = (byte) (pm >> 13);

    //the pathLength was in the bottom 13 bits
    short pathLength = (short) (pm & 8191); //0001111111111111 in binary

    return new PathMarker(pathLength, StringEncodings.find(encodingID));
  }

  public StringEncodings getPathEncoding() {
    return pathEncoding;
  }

  public short getPathLength() {
    return pathLength;
  }

  public int byteLength() {
    //a short combining the encoding and the length of the path string,
    //followed by the path string
    return 2 + pathLength;
  }
}
