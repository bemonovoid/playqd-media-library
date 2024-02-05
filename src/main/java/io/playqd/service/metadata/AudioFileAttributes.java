package io.playqd.service.metadata;

import jakarta.persistence.Transient;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public interface AudioFileAttributes {

  static boolean wasModified(Path absolutePath, AudioFileAttributes prevScannedAudioFile) {
    var newLastModifiedDate = Instant.ofEpochMilli(absolutePath.toFile().lastModified());
    return prevScannedAudioFile.getFileLastModifiedDate().isBefore(newLastModifiedDate);
  }

  @Deprecated
  static boolean wasModified(AudioFileAttributes prevScannedAudioFile) {
    var newLastModifiedDate = Instant.ofEpochMilli(prevScannedAudioFile.getPath().toFile().lastModified());
    return prevScannedAudioFile.getFileLastModifiedDate().isBefore(newLastModifiedDate);
  }

  long getId();

  String getLocation();

  String getExtension();

  long getSourceDirId();

  Instant getFileLastModifiedDate();

  @Transient
  default Path getPath() {
    return Paths.get(getLocation());
  }
}
