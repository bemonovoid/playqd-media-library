package io.playqd.service.watchfolder;

import io.playqd.exception.MediaSourceScannerException;
import io.playqd.model.event.AudioFileMetadataAddedEvent;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.WatchFolderDao;
import io.playqd.persistence.jpa.entity.AudioFileJpaEntity;
import io.playqd.service.metadata.AudioFileAttributes;
import io.playqd.service.metadata.FileAttributesToSqlParamsMapperFactory;
import io.playqd.service.metadata.ParamsMapperContext;
import io.playqd.util.SupportedAudioFiles;
import io.playqd.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class WatchFolderScannerImpl implements WatchFolderScanner {

  private final AudioFileDao audioFileDao;
  private final WatchFolderDao watchFolderDao;
  private final ApplicationEventPublisher eventPublisher;
  private final FileAttributesToSqlParamsMapperFactory fileAttributesToSqlParamsMapperFactory;

  public WatchFolderScannerImpl(AudioFileDao audioFileDao,
                                WatchFolderDao watchFolderDao,
                                ApplicationEventPublisher eventPublisher,
                                FileAttributesToSqlParamsMapperFactory fileAttributesToSqlParamsMapperFactory) {
    this.audioFileDao = audioFileDao;
    this.eventPublisher = eventPublisher;
    this.watchFolderDao = watchFolderDao;
    this.fileAttributesToSqlParamsMapperFactory = fileAttributesToSqlParamsMapperFactory;
  }

  @Override
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void scan(long watchFolderId) {
    scan(watchFolderId, null);
  }

  @Override
  @Async
  @Transactional(propagation = Propagation.REQUIRED)
  public void scan(long watchFolderId, Path subPath) {

    var watchFolder = watchFolderDao.get(watchFolderId);

    var scanPath = subPath != null ? subPath : watchFolder.path();

    try (Stream<Path> pathsStream = Files.walk(scanPath)) {

      var scanStartTime = LocalDateTime.now();

      Stream<AudioFileAttributes> prevScannedAudioFilesStream;

      if (subPath != null) {
        prevScannedAudioFilesStream = audioFileDao.streamByLocationStartsWith(subPath, AudioFileAttributes.class);
      } else {
        prevScannedAudioFilesStream =
            audioFileDao.streamByLocationStartsWith(watchFolder.path(), AudioFileAttributes.class);
      }

      var prevScannedAudioFiles = Collections.synchronizedMap(
          prevScannedAudioFilesStream.collect(Collectors.toMap(AudioFileAttributes::getPath, value -> value)));

      var newAudioFiles = Collections.synchronizedList(new LinkedList<Map<String, Object>>());
      var modifiedAudioFiles = Collections.synchronizedMap(new LinkedHashMap<Long, Map<String, Object>>());

      var totalFilesScanned = new AtomicLong(0);

      var fileAttributesMapper = fileAttributesToSqlParamsMapperFactory.get();

      pathsStream
          .parallel()
          .filter(ignoredDirs(watchFolder.ignoredDirectories()))
          .filter(SupportedAudioFiles::isSupportedAudioFile)
          .forEach(p -> {
            totalFilesScanned.incrementAndGet();
            var prevScannedAudioFile = prevScannedAudioFiles.remove(p);
            var mapperContext = new ParamsMapperContext(watchFolder, p);
            if (prevScannedAudioFile != null) {
              if (AudioFileAttributes.wasModified(p, prevScannedAudioFile)) {
                modifiedAudioFiles.put(prevScannedAudioFile.getId(), fileAttributesMapper.toSqlParams(mapperContext));
              }
              // Do nothing. Files wasn't modified and had already been scanned.
            } else {
              newAudioFiles.add(fileAttributesMapper.toSqlParams(mapperContext));
            }
          });

      var obsoleteAudioFiles = prevScannedAudioFiles.values().stream().map(AudioFileAttributes::getId).toList();

      deleteObsoleteMetadata(obsoleteAudioFiles);
      addNewMetadata(newAudioFiles);
      updateModifiedMetadata(modifiedAudioFiles);

      var scanDuration = Duration.between(scanStartTime.toLocalTime(), LocalTime.now());
      var duration = TimeUtils.durationToDisplayString(scanDuration);

      log.info(getReportStringTemplate(),
          watchFolder,
          newAudioFiles.size(),
          modifiedAudioFiles.size(),
          obsoleteAudioFiles.size(),
          duration,
          totalFilesScanned.get());

    } catch (IOException e) {
      throw new MediaSourceScannerException(e);
    }
  }

  private void deleteObsoleteMetadata(List<Long> ids) {
    if (ids.isEmpty()) {
      return;
    }
    var rowsDeleted = audioFileDao.deleteAllByIds(ids);
    log.info("Successfully deleted {} missing items", rowsDeleted);
  }

  private void addNewMetadata(List<Map<String, Object>> newItemsInsertParams) {
    if (newItemsInsertParams.isEmpty()) {
      return;
    }

    var isFirstScan = audioFileDao.isEmpty();

    audioFileDao.insertAll(newItemsInsertParams);

    var latestAddedDate = audioFileDao.findLatestAddedToWatchFolderDate();

    var newOldestAddedDate = newItemsInsertParams.stream()
        .map(map -> map.get(AudioFileJpaEntity.COL_FILE_ADDED_TO_WATCH_FOLDER_DATE))
        .map(LocalDate.class::cast)
        .sorted() // ASC sorting by default. If there were 3 missed additions, take the oldest date
        .findFirst()
        .orElse(latestAddedDate);

    if (isFirstScan) {
      newOldestAddedDate = latestAddedDate;
    }

    eventPublisher.publishEvent(new AudioFileMetadataAddedEvent(newItemsInsertParams.size(), newOldestAddedDate));
  }

  private void updateModifiedMetadata(Map<Long, Map<String, Object>> modifiedItemsInsertParams) {
    if (modifiedItemsInsertParams.isEmpty()) {
      return;
    }
    audioFileDao.updateAll(modifiedItemsInsertParams);
  }

  private static Predicate<Path> ignoredDirs(Set<String> ignoredDirs) {
    if (CollectionUtils.isEmpty(ignoredDirs)) {
      return path -> true;
    }
    return path -> !ignoredDirs.contains(path.getParent().getFileName().toString());
  }

  private static String getReportStringTemplate() {
    return """
                    
                    
        <-------------SCAN REPORT START------------->
                  
        {}
                               
        ------------
        files added:    {}
        files modified: {}
        files deleted:  {}
        ------------
        Duration:       {}
        Files scanned:  {}
                    
        <-------------SCAN REPORT END--------------->
        """;
  }
}