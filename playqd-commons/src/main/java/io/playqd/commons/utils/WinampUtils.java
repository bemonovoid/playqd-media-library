package io.playqd.commons.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WinampUtils {

  public static void main(String[] args) {
    var datFile = new File("C:\\Users\\gregory.kosik\\AppData\\Roaming\\Winamp\\Plugins\\ml\\main.dat");
    try (var is = new BufferedInputStream(new FileInputStream(datFile))) {
      var offset = 8;
      var headerBytes = new byte[10000];
      is.read(headerBytes, 0, 3);
//      System.out.println(new String(headerBytes));
      is.read(headerBytes, 3, 9255);

      System.out.println(new String(headerBytes));
//      var fieldId  = new byte[1];
//      offset++;
//      is.read(headerBytes, 0, offset);
//      offset++;
//      is.read(headerBytes, 0, offset);


    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  class WField {
    private byte id;
    private byte type;
  }
}
