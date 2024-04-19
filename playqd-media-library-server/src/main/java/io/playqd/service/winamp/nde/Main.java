package io.playqd.service.winamp.nde;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.stream.Collectors;

public class Main {


  public static void main(String[] args) throws Exception {

    System.out.println("i !will write peace on. your wings, and you will fly over_ the world".replaceAll("[^\\wA-Za-z0-9]",""));

    var ndeData = NdeData.read("C:\\Users\\gregory.kosik\\AppData\\Roaming\\Winamp");

    var res = ndeData.getRecords().stream()
//        .filter(ndeTrackRecord -> ndeTrackRecord.playCount() > 0)
        .filter(ndeTrackRecord -> ndeTrackRecord.mimeType() != null)
//        .sorted(Comparator.comparing(NdeTrackRecord::playCount).reversed())
        .collect(Collectors.groupingBy(ndeTrackRecord -> ndeTrackRecord.mimeType()));


    try (var fos = new FileOutputStream("data.ser"); var oos = new ObjectOutputStream(fos)) {
      oos.writeObject(ndeData.getRecords());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try(var fis = new FileInputStream("data.ser"); var ois = new ObjectInputStream(fis)) {
      var data = (List<NdeTrackRecord>) ois.readObject();
      System.out.println(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}