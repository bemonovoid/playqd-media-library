package io.playqd.persistence.projection;

import io.playqd.persistence.projection.AudioFileProjection;
import io.playqd.persistence.projection.AudioFileWithLastModifiedDate;
import jakarta.persistence.Transient;

import java.nio.file.Path;
import java.time.Instant;

public sealed interface WithLastModifiedDate extends AudioFileProjection permits AudioFileWithLastModifiedDate {

  Instant fileLastModifiedDate();

  @Transient
  default boolean wasModified(Path p) {
    if (!path().equals(p)) {
      return false;
    }
    var lastModifiedDate = Instant.ofEpochMilli(p.toFile().lastModified());
    return fileLastModifiedDate().toEpochMilli() < p.toFile().lastModified();
  }
}
