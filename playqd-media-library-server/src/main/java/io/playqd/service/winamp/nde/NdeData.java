package io.playqd.service.winamp.nde;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Getter
public class NdeData {

  private final List<NdeTrackRecord> records;

  private NdeData(List<NdeTrackRecord> records) {
    this.records = Collections.unmodifiableList(records);
  }

  public static NdeData from(List<NdeTrackRecord> records) {
    return new NdeData(records);
  }

  public static NdeData read(String winampRootFolder) throws IOException {
    var ndeFilesPath = Paths.get(winampRootFolder, "Plugins", "ml");

    if (!Files.exists(ndeFilesPath)) {
      throw new IllegalArgumentException(
          String.format("Can't find path: '%s' in path: '%s' ", "/Plugins/ml", winampRootFolder));
    }

    var mainDat = Paths.get(ndeFilesPath.toString(), "main.dat");
    var mainIdx = Paths.get(ndeFilesPath.toString(), "main.idx");

    return read(mainDat.toString(), mainIdx.toString());
  }

  public static NdeData read(String mainDatLocation, String mainIdxLocation) throws IOException {
    var ndeIndex = NdeIndex.read(mainIdxLocation);

    var byteBuffer = (ByteBuffer) null;

    try (var fis = new FileInputStream(mainDatLocation)) {
      byteBuffer = ByteBuffer.wrap(fis.readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    var valueExtractor = new ValueExtractor();

    validateHeader(byteBuffer);

    var columns = readColumnFields(byteBuffer, valueExtractor);

    readIndexFields(byteBuffer, valueExtractor);

    var records = readRecords(byteBuffer, columns, ndeIndex, valueExtractor);

    return new NdeData(records);
  }

  private static void validateHeader(ByteBuffer buffer) {
    var headerArr = ByteBuffer.allocate(8).array();
    buffer.get(headerArr);
    var dataHeaderId = "NDETABLE";
    var header = new String(headerArr);
    if (!dataHeaderId.equals(header)) {
      throw new IllegalStateException(
          String.format("Unexpected header, expected: %s, but was: %s.", dataHeaderId, header));
    }
  }

  private static Map<Byte, ColumnName> readColumnFields(ByteBuffer byteBuffer, ValueExtractor valueExtractor) {
    var columns = new HashMap<Byte, ColumnName>();
    var nextOffset = -1;
    while (nextOffset != 0) {
      var ndeField = readNdeField(byteBuffer);
      var value = valueExtractor.extract(ndeField);

      if (FieldTypes.COLUMN.fldType != ndeField.type) {
        System.out.println("Reached non COLUMN field type. Column read completed exceptionally.");
        break;
      }

      columns.putIfAbsent(ndeField.id, ColumnName.valueOf(value.toString()));

      nextOffset = ndeField.next;
    }
    return columns;
  }

  private static void readIndexFields(ByteBuffer byteBuffer, ValueExtractor valueExtractor) {
    var nextOffset = -1;
    while (nextOffset != 0) {
      var ndeField = readNdeField(byteBuffer);
      var value = valueExtractor.extract(ndeField);

      System.out.println(String.format("INDEX FIELD: %s", value));

      if (FieldTypes.INDEX.fldType != ndeField.type) {
        System.out.println("Reached INDEX field type. Column read completed.");
        break;
      }

      nextOffset = ndeField.next;
    }
  }

  private static List<NdeTrackRecord> readRecords(ByteBuffer buffer,
                                                  Map<Byte, ColumnName> columns,
                                                  NdeIndex ndeIndex,
                                                  ValueExtractor valueExtractor) {

    var indices = ndeIndex.getIndices();

    if (indices.isEmpty()) {
      return Collections.emptyList();
    }

    var numOfRecords = ndeIndex.getNumberOfRecords();
    var result = new ArrayList<NdeTrackRecord>(numOfRecords);

    if (indices.peek() != null && indices.peek().id() == 255) {
      indices.poll(); // skip primary
    }

    while (!indices.isEmpty()) {
      var nextOffset = -1;

      var idx = indices.poll();

      buffer.position(idx.offset());

      var recordItem = new HashMap<ColumnName, Object>(columns.size());

      while (nextOffset != 0) {
        var ndeField = readNdeField(buffer);
        var value = valueExtractor.extract(ndeField);
        recordItem.put(columns.get(ndeField.id), value);
        nextOffset = ndeField.next;
      }

      result.add(TrackRecordSerializer.serialize(recordItem));

      recordItem.clear();
    }
    return result;
  }

  private static NdeField readNdeField(ByteBuffer byteBuffer) {
    var ndeField = new NdeField();

    ndeField.id = byteBuffer.get();
    ndeField.type = byteBuffer.get();
    ndeField.size = Integer.reverseBytes(byteBuffer.getInt());
    ndeField.next = Integer.reverseBytes(byteBuffer.getInt());
    ndeField.prev = Integer.reverseBytes(byteBuffer.getInt());

    var dataArr = ByteBuffer.allocate(ndeField.size).array();

    byteBuffer.get(dataArr);

    ndeField.data = dataArr;

    return ndeField;
  }
}
