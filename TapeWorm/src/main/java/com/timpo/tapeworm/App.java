package com.timpo.tapeworm;

import com.timpo.tapeworm.enums.Checksums;
import com.timpo.tapeworm.enums.StringEncodings;
import com.timpo.tapeworm.other.Utils;
import com.google.common.io.ByteStreams;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import org.apache.commons.codec.binary.Hex;

/**
 * Hello world!
 *
 */
public class App {

  public static void readBashScript() throws IOException {
    BufferedReader stdOut = null;
    try {
      Process proc = Runtime.getRuntime().exec("/home/destino/workspace/JavaProject/listing.sh /"); //Whatever you want to execute
      stdOut = new BufferedReader(new InputStreamReader(proc.getInputStream()));

      try {
        proc.waitFor();
      } catch (InterruptedException e) {
        System.out.println(e.getMessage());
      }
      while (stdOut.ready()) {
        System.out.println(stdOut.readLine());
      }

    } catch (IOException e) {
      System.out.println(e.getMessage());

    } finally {
      if (stdOut != null) {
        stdOut.close();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    File fileIn = new File("empty.dat");
    File fileOut = new File("empty.tw");

    InputStream toReadFrom = new FileInputStream(fileIn);
    OutputStream toWriteTo = new FileOutputStream(fileOut);

    String filePath = "empty.dat";

    byte spanIndex = 0;
    long fileSize = 10;
    long bytesAlreadyWritten = 0;
    long outputBytesRemaining = Utils.twoToThe(30);

    try {
      TapeWormWriter.writeTapeWormFile(toWriteTo, toReadFrom, filePath, StringEncodings.UTF_16BE, spanIndex, fileSize, bytesAlreadyWritten, outputBytesRemaining, Checksums.MD5);

    } finally {
      toReadFrom.close();
      toWriteTo.close();
    }

    byte[] b = new byte[(int) fileOut.length()];
    InputStream in = new FileInputStream(fileOut);
    try {
      ByteStreams.readFully(in, b);
      System.out.println("in = " + Hex.encodeHexString(b));
    } finally {
      in.close();
    }
  }
}
