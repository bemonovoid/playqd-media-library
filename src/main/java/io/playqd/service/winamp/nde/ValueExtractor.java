package io.playqd.service.winamp.nde;

import io.playqd.service.winamp.nde.FieldTypes;
import io.playqd.service.winamp.nde.NdeField;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

class ValueExtractor {

  private int dataSize;
  private byte[] dataArray;
  private ByteBuffer dataBuffer;

  Object extract(NdeField ndeField) {

    var fieldType = FieldTypes.fromFldType(ndeField.type);

    dataBuffer = ByteBuffer.wrap(ndeField.data);

    switch (fieldType) {
      case COLUMN:
        // Skip first 2 bytes
        dataBuffer.get();
        dataBuffer.get();
        dataSize = Byte.toUnsignedInt(dataBuffer.get());
        dataArray = ByteBuffer.allocate(dataSize).array();
        dataBuffer.get(dataArray);
        return new String(dataArray);
      case INTEGER:
      case LENGTH:
        return Integer.reverseBytes(dataBuffer.getInt());
      case LONG:
        return Long.reverseBytes(dataBuffer.getLong());
      case DATETIME:
        var instant = Instant.ofEpochMilli(Integer.reverseBytes(dataBuffer.getInt()) * 1000L);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
      case STRING:
      case FILENAME:
        var size = Short.reverseBytes(dataBuffer.getShort());
        dataArray = ByteBuffer.allocate(size).array();
        dataBuffer.get(dataArray);
        return new String(dataArray, StandardCharsets.UTF_16LE);
      case INDEX:
        // skip 8 bytes
        dataBuffer.getLong();
        dataSize = Byte.toUnsignedInt(dataBuffer.get());
        dataArray = ByteBuffer.allocate(dataSize).array();
        dataBuffer.get(dataArray);
        return new String(dataArray);
      default:
        throw new RuntimeException(String.format("Failed extracting value for nde field: %s", fieldType));
    }
  }
}
