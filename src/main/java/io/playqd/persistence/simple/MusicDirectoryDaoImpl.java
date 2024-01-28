package io.playqd.persistence.simple;

import io.playqd.commons.data.MusicDirectory;
import io.playqd.exception.PlayqdException;
import io.playqd.persistence.MusicDirectoryDao;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MusicDirectoryDaoImpl implements MusicDirectoryDao {

  private final Map<Long, MusicDirectory> musicDirectories = new ConcurrentHashMap<>();

  @Override
  public List<MusicDirectory> getAll() {
    return new ArrayList<>(musicDirectories.values());
  }

  @Override
  public MusicDirectory get(long id) {
    return musicDirectories.get(id);
  }

  @Override
  public MusicDirectory create(MusicDirectory musicDirectory) {
    if (musicDirectories.containsKey(musicDirectory.id())) {
      throw new PlayqdException(String.format("Media source already exists. %s", musicDirectory));
    }
    return musicDirectories.put(musicDirectory.id(), musicDirectory);
  }
}
