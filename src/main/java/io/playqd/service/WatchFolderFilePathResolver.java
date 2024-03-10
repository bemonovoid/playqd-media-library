package io.playqd.service;

import io.playqd.commons.data.WatchFolder;
import io.playqd.model.AudioFile;
import io.playqd.service.metadata.AudioFileAttributes;

import java.nio.file.Path;
import java.util.Optional;

public interface WatchFolderFilePathResolver {

  Optional<WatchFolder> resolveWatchFolder(Path path);

  Path relativize(Path path);

  Path relativize(long watchFolderId, Path other);

  default Path relativize(Path parentPath, Path other) {
    return parentPath.relativize(other);
  }

  default Path unRelativize(AudioFileAttributes audioFile) {
    return unRelativize(audioFile.getSourceDirId(), audioFile.getPath());
  }

  default Path unRelativize(AudioFile audioFile) {
    return unRelativize(audioFile.sourceDirId(), audioFile.path());
  }

  Path unRelativize(Path other);

  Path unRelativize(long musicDirId, Path other);
}
