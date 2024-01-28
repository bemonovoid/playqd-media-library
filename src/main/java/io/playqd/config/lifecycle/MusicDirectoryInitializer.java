package io.playqd.config.lifecycle;

import io.playqd.commons.data.MusicDirectory;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.service.mediasource.MusicDirectoryScanner;
import io.playqd.service.mediasource.MusicDirectoryManager;
import io.playqd.service.mediasource.MusicDirectoryWatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MusicDirectoryInitializer implements ApplicationRunner {

  private final PlayqdProperties playqdProperties;
  private final MusicDirectoryManager musicDirectoryManager;
  private final MusicDirectoryScanner musicDirectoryScanner;
  private final MusicDirectoryWatchService musicDirectoryWatchService;

  public MusicDirectoryInitializer(PlayqdProperties playqdProperties,
                                   MusicDirectoryManager musicDirectoryManager,
                                   MusicDirectoryScanner musicDirectoryScanner,
                                   MusicDirectoryWatchService musicDirectoryWatchService) {
    this.playqdProperties = playqdProperties;
    this.musicDirectoryManager = musicDirectoryManager;
    this.musicDirectoryScanner = musicDirectoryScanner;
    this.musicDirectoryWatchService = musicDirectoryWatchService;
  }

  @Override
  public void run(ApplicationArguments args) {

    initMusicDirectories();

    musicDirectoryManager.getAll().forEach(musicDirectory -> {
      if (musicDirectory.autoScanOnStartUp()) {
        log.info("Re-scanning media source {} ...", musicDirectory);
        musicDirectoryScanner.scan(musicDirectory.id());
      }
      if (musicDirectory.watchable()) {
        musicDirectoryWatchService.watch(musicDirectory);
      }
    });
  }

  private void initMusicDirectories() {
    var idGenerator = new AtomicLong(1);
    this.playqdProperties.getSources()
        .forEach(config -> this.musicDirectoryManager.create(
            new MusicDirectory(
                idGenerator.getAndIncrement(),
                config.getName(),
                Paths.get(config.getPath()),
                config.isScanOnStart(),
                config.isWatchable(),
                config.getIgnoreDirs())));
  }

}
