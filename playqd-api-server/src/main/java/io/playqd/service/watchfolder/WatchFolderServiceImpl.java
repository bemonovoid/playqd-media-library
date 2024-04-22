package io.playqd.service.watchfolder;

import io.playqd.commons.data.WatchFolderContentInfo;
import io.playqd.exception.CounterException;
import io.playqd.persistence.WatchFolderDao;
import io.playqd.util.SupportedAudioFiles;
import io.playqd.commons.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class WatchFolderServiceImpl implements WatchFolderService {

  private final WatchFolderDao watchFolderDao;

  public WatchFolderServiceImpl(WatchFolderDao watchFolderDao) {
    this.watchFolderDao = watchFolderDao;
  }

  @Override
  public WatchFolderContentInfo info(long sourceId) {
    var mediaSource = watchFolderDao.get(sourceId);
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
      return new WatchFolderContentInfo(mediaSource, totalCount, extensionCounts);
    } catch (IOException e) {
      throw new CounterException(e);
    }
  }
}