package io.playqd.service.winamp.nde;

import lombok.AccessLevel;
import lombok.Getter;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

@Getter(AccessLevel.PACKAGE)
class NdeIndex {

  private final int numberOfRecords;
  private final Queue<Index> indices;

  private NdeIndex(int numberOfRecords, Queue<Index> indices) {
    this.numberOfRecords = numberOfRecords;
    this.indices = indices;
  }

  static NdeIndex read(String location) {
    try (var dis = new DataInputStream(new FileInputStream(location))) {
      validateHeader(dis);
      var numberOfRecords = readNumberOfRecords(dis);
      return new NdeIndex(numberOfRecords, readIndices(dis, numberOfRecords));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void validateHeader(DataInputStream dis) throws IOException {
    var headerArr = ByteBuffer.allocate(8).array();
    var read = dis.read(headerArr);
    if (read == 0) {
      throw new IllegalStateException("File header was empty");
    }
    var indexHeaderId = "NDEINDEX";
    var header = new String(headerArr);
    if (!indexHeaderId.equals(header)) {
      throw new IllegalStateException(
          String.format("Unexpected header, expected: %s, but was: %s", indexHeaderId, header));
    }
  }

  private static int readNumberOfRecords(DataInputStream dis) throws IOException {
    return Integer.reverseBytes(dis.readInt());
  }

  private static Queue<Index> readIndices(DataInputStream dis, int numberOfRecords) throws IOException {
    var indices = new LinkedList<Index>();

    while (numberOfRecords != 0) {
      var id = Integer.reverseBytes(dis.readInt());
      var offset = Integer.reverseBytes(dis.readInt());
      indices.add(new Index(id, offset));
      numberOfRecords --;
    }
    return indices;
  }
}
