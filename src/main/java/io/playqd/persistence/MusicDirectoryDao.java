package io.playqd.persistence;

import io.playqd.commons.data.MusicDirectory;

import java.util.List;

public interface MusicDirectoryDao {

  MusicDirectory get(long id);

  List<MusicDirectory> getAll();

  MusicDirectory create(MusicDirectory musicDirectory);

}
