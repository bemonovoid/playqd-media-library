package io.playqd.service;

import io.playqd.service.metadata.MetadataFile;
import lombok.Getter;

import java.util.Optional;
import java.util.stream.Stream;

public interface MetadataFileReader {

  MetadataFile findAndReadCueSheetFromDir(String dir);

  @Getter
  enum Formats {

    CUE("cue");

    private final String format;

    Formats(String format) {
      this.format = format;
    }

    static Optional<Formats> fromFormat(String formatString) {
      return Stream.of(Formats.values())
          .filter(format -> format.getFormat().equalsIgnoreCase(formatString))
          .findFirst();
    }

  }

}
