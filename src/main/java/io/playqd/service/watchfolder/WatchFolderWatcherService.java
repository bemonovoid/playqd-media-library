package io.playqd.service.watchfolder;

import io.playqd.commons.data.WatchFolder;

public interface WatchFolderWatcherService {

  void watch(WatchFolder watchFolder);

  void stop(long sourceId);
}
