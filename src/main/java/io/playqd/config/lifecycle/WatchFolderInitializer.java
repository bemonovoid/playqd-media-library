package io.playqd.config.lifecycle;

import io.playqd.persistence.WatchFolderDao;
import io.playqd.service.watchfolder.WatchFolderScanner;
import io.playqd.service.watchfolder.WatchFolderWatcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

@Slf4j
public class WatchFolderInitializer implements ApplicationRunner {

  private final WatchFolderDao watchFolderDao;
  private final WatchFolderScanner watchFolderScanner;
  private final WatchFolderWatcherService watchFolderWatcherService;

  public WatchFolderInitializer(WatchFolderDao watchFolderDao,
                         WatchFolderScanner watchFolderScanner,
                         WatchFolderWatcherService watchFolderWatcherService) {
    this.watchFolderDao = watchFolderDao;
    this.watchFolderScanner = watchFolderScanner;
    this.watchFolderWatcherService = watchFolderWatcherService;
  }

  @Override
  public void run(ApplicationArguments args) {
    watchFolderDao.getAll().forEach(watchFolder -> {
      if (watchFolder.autoScanOnStartUp()) {
        log.info("Re-scanning folder {} ...", watchFolder);
        watchFolderScanner.scan(watchFolder.id());
      }
      if (watchFolder.watchable()) {
        watchFolderWatcherService.watch(watchFolder);
      }
    });
  }
}