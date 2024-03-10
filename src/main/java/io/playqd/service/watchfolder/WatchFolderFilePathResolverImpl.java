package io.playqd.service.watchfolder;

import io.playqd.commons.data.WatchFolder;
import io.playqd.persistence.WatchFolderDao;
import io.playqd.service.WatchFolderFilePathResolver;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

@Component
class WatchFolderFilePathResolverImpl implements WatchFolderFilePathResolver {

  private final WatchFolderDao watchFolderDao;

  WatchFolderFilePathResolverImpl(WatchFolderDao watchFolderDao) {
    this.watchFolderDao = watchFolderDao;
  }

  @Override
  public Optional<WatchFolder> resolveWatchFolder(Path path) {
    return watchFolderDao.getAll().stream()
        .filter(watchFolder -> path.startsWith(watchFolder.path()))
        .findFirst();
  }

  @Override
  public Path relativize(Path path) {
    return resolveWatchFolder(path)
        .map(watchFolder -> watchFolder.path().relativize(path))
        .orElse(null);
  }

  @Override
  public Path relativize(long watchFolderId, Path other) {
    return relativize(watchFolderDao.get(watchFolderId).path(), other);
  }

  @Override
  public Path unRelativize(Path path) {
    return watchFolderDao.getAll().stream()
        .map(WatchFolder::path)
        .map(watchFolderPath -> unRelativize(watchFolderPath, path))
        .findFirst()
        .orElse(null);
  }

  @Override
  public Path unRelativize(long musicDirId, Path other) {
    return unRelativize(watchFolderDao.get(musicDirId).path(), other);
  }

  private static Path unRelativize(Path path, Path other) {
    return path.resolve(other);
  }
}