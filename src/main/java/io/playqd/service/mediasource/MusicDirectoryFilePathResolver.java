package io.playqd.service.mediasource;

import io.playqd.commons.data.MusicDirectory;
import io.playqd.persistence.MusicDirectoryDao;
import io.playqd.service.AudioFilePathResolver;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

@Component
class MusicDirectoryFilePathResolver implements AudioFilePathResolver {

  private final MusicDirectoryDao musicDirectoryDao;

  MusicDirectoryFilePathResolver(MusicDirectoryDao musicDirectoryDao) {
    this.musicDirectoryDao = musicDirectoryDao;
  }

  @Override
  public Optional<MusicDirectory> resolveSourceDir(Path path) {
    return musicDirectoryDao.getAll().stream()
        .filter(musicDirectory -> path.startsWith(musicDirectory.path()))
        .findFirst();
  }

  @Override
  public Path relativize(Path path) {
    return resolveSourceDir(path)
        .map(musicDirectory -> musicDirectory.path().relativize(path))
        .orElse(null);
  }

  @Override
  public Path relativize(long musicDirId, Path other) {
    return relativize(musicDirectoryDao.get(musicDirId).path(), other);
  }

  @Override
  public Path unRelativize(long musicDirId, Path other) {
    return musicDirectoryDao.get(musicDirId).path().resolve(other);
  }
}
