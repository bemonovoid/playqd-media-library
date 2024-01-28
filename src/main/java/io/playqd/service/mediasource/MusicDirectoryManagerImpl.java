package io.playqd.service.mediasource;

import io.playqd.commons.data.MusicDirectory;
import io.playqd.commons.data.MusicDirectoryContentInfo;
import io.playqd.exception.CounterException;
import io.playqd.persistence.MusicDirectoryDao;
import io.playqd.util.FileUtils;
import io.playqd.util.SupportedAudioFiles;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MusicDirectoryManagerImpl implements MusicDirectoryManager {

  private final MusicDirectoryDao musicDirectoryDao;

  public MusicDirectoryManagerImpl(MusicDirectoryDao musicDirectoryDao) {
    this.musicDirectoryDao = musicDirectoryDao;
  }

  @Override
  public MusicDirectory get(long sourceId) {
    return musicDirectoryDao.get(sourceId);
  }

  @Override
  public List<MusicDirectory> getAll() {
    return musicDirectoryDao.getAll();
  }

  @Override
  public MusicDirectory create(MusicDirectory musicDirectory) {
    return musicDirectoryDao.create(musicDirectory);
  }

  @Override
  public MusicDirectoryContentInfo info(long sourceId) {
    var mediaSource = get(sourceId);
    try (Stream<Path> filesStream = Files.walk(mediaSource.path())) {
      var extensionCounts = filesStream
          .filter(SupportedAudioFiles::isSupportedAudioFile)
          .collect(Collectors.groupingBy(FileUtils::getFileExtension, Collectors.counting()))
          .entrySet().stream()
          .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue,
              (e1, e2) -> e1,
              LinkedHashMap::new));
      var totalCount = extensionCounts.values().stream()
          .collect(Collectors.summarizingLong(value -> value))
          .getSum();
      return new MusicDirectoryContentInfo(mediaSource, totalCount, extensionCounts);
    } catch (IOException e) {
      throw new CounterException(e);
    }
  }

}
