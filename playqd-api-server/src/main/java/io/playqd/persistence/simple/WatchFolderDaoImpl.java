package io.playqd.persistence.simple;

import io.playqd.commons.data.WatchFolder;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.exception.PlayqdException;
import io.playqd.persistence.WatchFolderDao;
import io.playqd.util.UUIDV3Ids;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public class WatchFolderDaoImpl implements WatchFolderDao {

  private final Map<Long, WatchFolder> watchFolders;

  public WatchFolderDaoImpl(PlayqdProperties playqdProperties) {
    var idGenerator = new AtomicLong(1);
    this.watchFolders = playqdProperties.getFolders()
        .stream().map(config ->
            new WatchFolder(
                idGenerator.getAndIncrement(),
                UUIDV3Ids.create(Paths.get(config.getPath()).toString()),
                config.getName(),
                Paths.get(config.getPath()),
                config.isScanOnStart(),
                config.isWatchable(),
                config.getIgnoreDirs()))
        .collect(Collectors.toMap(WatchFolder::id, value -> value));
  }

  @Override
  public List<WatchFolder> getAll() {
    return new ArrayList<>(watchFolders.values());
  }

  @Override
  public boolean contains(long id) {
    return watchFolders.containsKey(id);
  }

  @Override
  public boolean contains(String uuid) {
    return watchFolders.values().stream().anyMatch(watchFolder -> watchFolder.uuid().equals(uuid));
  }

  @Override
  public int count() {
    return watchFolders.size();
  }

  @Override
  public WatchFolder get(long id) {
    return watchFolders.get(id);
  }

  @Override
  public WatchFolder get(String uuid) {
    return watchFolders.values().stream()
        .filter(watchFolder -> watchFolder.uuid().equals(uuid))
        .findFirst()
        .orElse(null);
  }

  @Override
  public WatchFolder create(WatchFolder watchFolder) {
    if (watchFolders.containsKey(watchFolder.id())) {
      throw new PlayqdException(String.format("Watch folder already exists. %s", watchFolder));
    }
    return watchFolders.put(watchFolder.id(), watchFolder);
  }
}
