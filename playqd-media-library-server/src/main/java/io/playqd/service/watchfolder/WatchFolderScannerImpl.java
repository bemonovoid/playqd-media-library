package io.playqd.service.watchfolder;

import io.playqd.commons.data.WatchFolder;
import io.playqd.commons.utils.Tuple;
import io.playqd.exception.WatchFolderScanException;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.WatchFolderDao;
import io.playqd.persistence.projection.AudioFileWithLastModifiedDate;
import io.playqd.service.metadata.FileAttributesToSqlParamsMapperFactory;
import io.playqd.util.SupportedAudioFiles;
import io.playqd.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class WatchFolderScannerImpl implements WatchFolderScanner {

  private final AudioFileDao audioFileDao;
  private final WatchFolderDao watchFolderDao;
  private final FileAttributesToSqlParamsMapperFactory fileAttributesMapperFactory;

  public WatchFolderScannerImpl(AudioFileDao audioFileDao,
                                WatchFolderDao watchFolderDao,
                                FileAttributesToSqlParamsMapperFactory fileAttributesMapperFactory) {
    this.audioFileDao = audioFileDao;
    this.watchFolderDao = watchFolderDao;
    this.fileAttributesMapperFactory = fileAttributesMapperFactory;
  }

  @Override
  @Async
  @Transactional(propagation = Propagation.REQUIRED)
  public void scan(long dirId) {
    scan(dirId, null);
  }

  @Override
  @Async
  @Transactional(propagation = Propagation.REQUIRED)
  public void scan(long dirId, Path subPath) {
    var watchFolder = watchFolderDao.get(dirId);

    var scanPath = subPath != null ? subPath : watchFolder.path();

    var databaseAudioFiles =
        audioFileDao.getDataByLocationStartsWith(scanPath, AudioFileWithLastModifiedDate.class).getContent().stream()
            .collect(Collectors.toMap(AudioFileWithLastModifiedDate::path, value -> value));

    var fileAttributesMapper = fileAttributesMapperFactory.get();

    var scanStartTime = LocalDateTime.now();

    var scanStats = ScanStats.createDefault();

    try (var virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
      try (var pathsStream = Files.walk(scanPath)) {
        Queue<CompletableFuture<?>> scans = pathsStream
            .filter(ignoredDirs(watchFolder.ignoredDirectories()))
            .filter(SupportedAudioFiles::isSupportedAudioFile)
            .peek(path -> scanStats.totalFound.incrementAndGet())
            .map(p -> Tuple.from(p, databaseAudioFiles.remove(p)))
            .filter(t -> t.right() == null || t.right().wasModified(t.left()))
            .map(t -> Tuple.from(t.left(), t.right() != null))
            .map(pathAndWasModified -> CompletableFuture
                .supplyAsync(() -> {
                      var audioFile = readAudioFile(pathAndWasModified.left());
                      if (audioFile == null) {
                        log.error("Sync skipped for {}. Failed to read AudioFile from it.", pathAndWasModified.left());
                        scanStats.totalFailed().incrementAndGet();
                        return Tuple.<AudioFile, Boolean>empty();
                      }
                      scanStats.totalRead().incrementAndGet();
                      return Tuple.from(audioFile, pathAndWasModified.right());
                    },
                    virtualThreadExecutor)
                .thenApplyAsync(audioFileAndWasModified -> {
                  if (audioFileAndWasModified.isEmpty()) {
                    return Tuple.<Map<String, Object>, Boolean>empty();
                  }
                  return Tuple.from(fileAttributesMapper.toSqlParams(
                      audioFileAndWasModified.left()), audioFileAndWasModified.right());
                }, virtualThreadExecutor)
                .thenApplyAsync(sqlParamsAndWasModified -> {
                  if (sqlParamsAndWasModified.isEmpty()) {
                    return -1;
                  }
                  if (sqlParamsAndWasModified.left().isEmpty()) {
                    scanStats.totalFailed().incrementAndGet();
                    return -1;
                  }
                  if (sqlParamsAndWasModified.right()) {
                    // update
//                    scanStats.totalLibraryModified().incrementAndGet()
                    return -1;
                  }
                  scanStats.totalMapped().incrementAndGet();
                  var rowsInserted = audioFileDao.insertOne(sqlParamsAndWasModified.left());
                  if (rowsInserted > 0) {
                    scanStats.totalLibraryAdded().incrementAndGet();
                  } else {
                    scanStats.totalFailed().incrementAndGet();
                  }
                  return rowsInserted;
                }, virtualThreadExecutor)
                .whenCompleteAsync((effectedRows, throwable) -> {
                  if (throwable != null) {
                    log.error("Scan task failed exceptionally.", throwable);
                  }
                }))
            .collect(Collectors.toCollection(LinkedList::new));

        waitCompletion(scans);

        if (!databaseAudioFiles.isEmpty()) {
          var rowsDeleted = (int) audioFileDao.deleteAllByIds(databaseAudioFiles.values().stream()
              .map(AudioFileWithLastModifiedDate::id)
              .toList());
          scanStats.totalFailed().getAndAdd(databaseAudioFiles.size() - rowsDeleted);
          scanStats.totalLibraryRemoved().set(rowsDeleted);
        }

        var scanDuration = Duration.between(scanStartTime.toLocalTime(), LocalTime.now());
        log.info(getReportStringTemplate(watchFolder, scanStats, scanDuration));

//
      } catch (IOException e) {
        throw new WatchFolderScanException(e);
      }
    }
  }

  private static void waitCompletion(Queue<CompletableFuture<?>> futures) {
    while (!futures.isEmpty()) {
      if (futures.peek().state() != Future.State.RUNNING) {
        futures.poll();
      }
    }
  }

  private static Predicate<Path> ignoredDirs(Set<String> ignoredDirs) {
    if (CollectionUtils.isEmpty(ignoredDirs)) {
      return path -> true;
    }
    return path -> !ignoredDirs.contains(path.getParent().getFileName().toString());
  }

  private static AudioFile readAudioFile(Path path) {
    try {
      return AudioFileIO.read(path.toFile());
    } catch (CannotReadException | TagException | InvalidAudioFrameException | ReadOnlyFileException | IOException e) {
      log.error("AudioFile read error. {}", path, e);
      return null;
    }
  }

  private static String getReportStringTemplate(WatchFolder watchFolder, ScanStats scanStats, Duration duration) {
    return StringTemplate.STR."""

        <-------------SCAN REPORT------------->
                  
        \{ watchFolder }

        ------------
        files found:    \{ scanStats.totalFound().get() }
        files read:     \{ scanStats.totalRead().get() }
        files mapped:   \{ scanStats.totalMapped().get() }
        files failed:   \{ scanStats.totalFailed().get() }
        ------------
        files added:    \{ scanStats.totalLibraryAdded().get() }
        files modified: \{ scanStats.totalLibraryModified().get() }
        files deleted:  \{ scanStats.totalLibraryRemoved().get() }
        ------------
        Duration:       \{ TimeUtils.durationToDisplayString(duration) }
                    
        <-------------SCAN REPORT--------------->
        """;
  }

  record ScanStats(AtomicInteger totalFound,
                   AtomicInteger totalRead,
                   AtomicInteger totalMapped,
                   AtomicInteger totalFailed,
                   AtomicInteger totalLibraryAdded,
                   AtomicInteger totalLibraryModified,
                   AtomicInteger totalLibraryRemoved) {

    public static ScanStats createDefault() {
      return new ScanStats(
          new AtomicInteger(),
          new AtomicInteger(),
          new AtomicInteger(),
          new AtomicInteger(),
          new AtomicInteger(),
          new AtomicInteger(),
          new AtomicInteger());
    }
  } ;
}
