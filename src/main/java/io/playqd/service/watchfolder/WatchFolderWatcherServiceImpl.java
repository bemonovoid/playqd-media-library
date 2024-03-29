package io.playqd.service.watchfolder;

import com.sun.nio.file.ExtendedWatchEventModifier;
import io.playqd.commons.data.WatchFolder;
import io.playqd.model.event.WatchFolderModifiedEvent;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class WatchFolderWatcherServiceImpl implements WatchFolderWatcherService {

  private final ApplicationEventPublisher eventPublisher;

  private final Map<Long, WatchService> watchers = Collections.synchronizedMap(new HashMap<>());

  public WatchFolderWatcherServiceImpl(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Async
  @Override
  public void watch(WatchFolder watchFolder) {
    var watchFolderId = watchFolder.id();
    if (watchers.containsKey(watchFolderId)) {
      log.warn("WatchService for media source with id: {} is already enabled.", watchFolderId);
      return; //TODO log warning
    }

    Path sourcePath = watchFolder.path();

    if (!Files.exists(sourcePath)) {
      log.error("Path '{}' in source with id: '{}' does not exist", sourcePath, watchFolderId);
      return;
    }

    log.info("Enabling new WatcherService for media source with id: {}", watchFolderId);

    try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
      watchers.put(watchFolderId, watchService);
      log.info("WatcherService for media source with id: {} was registered and is now starting ...", watchFolderId);
      startWatcher(watchService, watchFolder, sourcePath);
    } catch (IOException e) {
      log.error("Failed to register new WatcherService for media source with id: {}. {}",
          watchFolderId, e.getMessage());
    }
  }

  @Async
  @Override
  public void stop(long sourceId) {
    WatchService watchService = watchers.remove(sourceId);
    if (watchService != null) {
      stopWatcher(sourceId, watchService);
    }
  }

  private void startWatcher(WatchService watchService, WatchFolder watchFolder, Path watchable) {

    WatchEvent.Kind<?>[] events = {
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_MODIFY,
        StandardWatchEventKinds.ENTRY_DELETE
    };

    try {
      watchable.register(watchService, events, ExtendedWatchEventModifier.FILE_TREE);
//            watchable.register(watchService, events);
    } catch (IOException e) {
      log.error("Unexpected error occurred while starting watcher at: {}.", watchable, e);
      return;
    }

    boolean poll = true;

    log.info("Started WatcherService at: '{}'. Watching events: {}", watchable, Arrays.toString(events));

    final Map<String, Set<Path>> watchedContent = new HashMap<>();

    while (poll) {

      WatchKey key;

      try {
        if (watchedContent.isEmpty()) {
          key = watchService.take();
        } else {
          key = watchService.poll(5, TimeUnit.SECONDS);
          if (key == null) {
            notifyWatchedContentChanged(watchFolder, watchedContent);
            continue;
          }
        }

      } catch (ClosedWatchServiceException e) {
        log.warn("The WatcherService is being closed, may be application is now exiting. {}", e.getMessage());
        break;
      } catch (InterruptedException e1) {
        log.error("Can't continue watching the path: {}. Watcher was interrupted.", watchable, e1);
        continue;
      }

      for (WatchEvent<?> event : key.pollEvents()) {

        WatchEvent.Kind<?> kind = event.kind();

        log.info("Received audio source watcher event: {}", kind.name());

        @SuppressWarnings("unchecked")
        WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;

        Path createdPath = watchEvent.context();

        log.info("Watcher event context: {}", createdPath.toString());

        Path resolvedPath = watchable.resolve(createdPath);

        log.info("Full watcher event context: {}", resolvedPath);

        watchedContent.computeIfAbsent(kind.name(), value -> new HashSet<>()).add(resolvedPath);
      }

      poll = key.reset();

    }

    notifyWatchedContentChanged(watchFolder, watchedContent);

    stop(watchFolder.id());
  }

  private void notifyWatchedContentChanged(WatchFolder watchFolder, Map<String, Set<Path>> watchedContent) {
    if (watchedContent.isEmpty()) {
      return;
    }
    Set<Path> changedContentDirs = watchedContent.values().stream()
        .flatMap(Collection::stream)
        .map(path -> {
          var parentPath = path;
          while (!parentPath.getParent().equals(watchFolder.path())) {
            parentPath = parentPath.getParent();
          }
          return parentPath;
        })
        .collect(Collectors.toSet());

    eventPublisher.publishEvent(new WatchFolderModifiedEvent(watchFolder, changedContentDirs));

    watchedContent.clear();
  }

  @PreDestroy
  private void stopAllBeforeExit() {
    log.info("Stopping {} WatcherService(s) before application termination ...", watchers.size());

    for (Map.Entry<Long, WatchService> watcherEntry : watchers.entrySet()) {
      var sourceId = watcherEntry.getKey();
      var watchService = watcherEntry.getValue();

      log.info("Stopping WatcherService for media source id: {}", sourceId);

      stopWatcher(sourceId, watchService);

      log.info("WatcherService for media source id: {} was successfully stopped", sourceId);
    }

    watchers.clear();

    log.info("All WatcherServices were unregistered");
  }

  private void stopWatcher(long sourceId, WatchService watchService) {
    try {
      WatchKey poll = watchService.poll();
      if (poll != null) {
        poll.cancel();
      }
      watchService.close();
    } catch (ClosedWatchServiceException e) {
      log.error("WatcherService for media source id: {} was already closed.", sourceId);
    } catch (IOException e) {
      log.error("Failed to gracefully stop WatcherService for media source id: {}. {}", sourceId, e.getMessage());
    }
  }

}
