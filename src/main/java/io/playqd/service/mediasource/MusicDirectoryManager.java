package io.playqd.service.mediasource;

import io.playqd.commons.data.MusicDirectory;
import io.playqd.commons.data.MusicDirectoryContentInfo;

import java.util.List;

public interface MusicDirectoryManager {

  List<MusicDirectory> getAll();

  MusicDirectory get(long sourceId);

  MusicDirectory create(MusicDirectory musicDirectory);

  MusicDirectoryContentInfo info(long sourceId);

}
