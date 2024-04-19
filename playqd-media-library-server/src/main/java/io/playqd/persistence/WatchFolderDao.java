package io.playqd.persistence;

import io.playqd.commons.data.WatchFolder;

import java.util.List;

public interface WatchFolderDao {

  boolean contains(long id);

  boolean contains(String uuid);

  int count();

  WatchFolder get(long id);

  WatchFolder get(String uuid);

  List<WatchFolder> getAll();

  WatchFolder create(WatchFolder watchFolder);
}