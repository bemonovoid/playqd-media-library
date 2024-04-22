package io.playqd.service.watchfolder;

import java.nio.file.Path;

public interface WatchFolderScanner {

  void scan(long dirId);

  void scan(long dirId, Path subPath);
}
