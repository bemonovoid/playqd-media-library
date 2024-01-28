package io.playqd.service.mediasource;


import io.playqd.commons.data.MusicDirectory;

public interface MusicDirectoryWatchService {

  void watch(MusicDirectory musicDirectory);

  void stop(long sourceId);
}
