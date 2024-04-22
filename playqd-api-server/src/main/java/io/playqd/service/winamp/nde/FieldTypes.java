package io.playqd.service.winamp.nde;

import java.util.EnumSet;

enum FieldTypes {

  UNDEFINED(255),
  COLUMN(0),
  INDEX(1),
  REDIRECTOR(2),
  STRING(3),
  INTEGER(4),
  BOOLEAN(5),
  BINARY(6),
  GUID(7),
  PRIVATE(8),
  FIELD_BITMAP(6),
  FLOAT(9),
  DATETIME(10),
  LENGTH(11),
  FILENAME(12),
  LONG(13),
  BINARY_32(14),
  INT_128(15);

  final int fldType;

  FieldTypes(int i) {
    this.fldType = i;
  }

  static FieldTypes fromFldType(int code) {
    var set = EnumSet.allOf(FieldTypes.class);
    return set.stream().filter(t -> t.fldType == code)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Unexpected type: " + code));
  }
}
