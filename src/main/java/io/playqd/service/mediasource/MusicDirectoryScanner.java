package io.playqd.service.mediasource;

import java.nio.file.Path;

public interface MusicDirectoryScanner {

  void scan(long dirId);

  void scan(long dirId, Path subPath);
}
