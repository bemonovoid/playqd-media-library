package io.playqd.config;

import io.playqd.config.lifecycle.ApplicationRunnerOrder;
import io.playqd.config.lifecycle.WatchFolderInitializer;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.WatchFolderDao;
import io.playqd.persistence.simple.WatchFolderDaoImpl;
import io.playqd.service.metadata.FileAttributesToSqlParamsMapper;
import io.playqd.service.metadata.FileAttributesToSqlParamsMapperFactory;
import io.playqd.service.watchfolder.WatchFolderBrowser;
import io.playqd.service.watchfolder.WatchFolderBrowserImpl;
import io.playqd.service.watchfolder.WatchFolderScanner;
import io.playqd.service.watchfolder.WatchFolderScannerImpl;
import io.playqd.service.watchfolder.WatchFolderService;
import io.playqd.service.watchfolder.WatchFolderServiceImpl;
import io.playqd.service.watchfolder.WatchFolderWatcherService;
import io.playqd.service.watchfolder.WatchFolderWatcherServiceImpl;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class WatchFolderContextConfiguration {

  @Bean
  WatchFolderDao watchFolderDao(PlayqdProperties playqdProperties) {
    return new WatchFolderDaoImpl(playqdProperties);
  }

  @Bean
  WatchFolderBrowser watchFolderBrowser(WatchFolderDao watchFolderDao) {
    return new WatchFolderBrowserImpl(watchFolderDao);
  }

  @Bean
  WatchFolderService watchFolderService(WatchFolderDao watchFolderDao) {
    return new WatchFolderServiceImpl(watchFolderDao);
  }

  @Bean
  WatchFolderScanner watchFolderScanner(AudioFileDao audioFileDao,
                                        WatchFolderDao watchFolderDao,
                                        ApplicationEventPublisher eventPublisher,
                                        FileAttributesToSqlParamsMapperFactory toSqlParamsMapperFactory) {
    return new WatchFolderScannerImpl(audioFileDao, watchFolderDao, eventPublisher, toSqlParamsMapperFactory);
  }

  @Bean
  WatchFolderWatcherService watchFolderWatcherService(ApplicationEventPublisher eventPublisher) {
    return new WatchFolderWatcherServiceImpl(eventPublisher);
  }

  @Bean
  @Order(ApplicationRunnerOrder.MEDIA_SOURCE_INITIALIZER)
  ApplicationRunner watchFolderInitializer(WatchFolderDao watchFolderDao,
                                           WatchFolderScanner watchFolderScanner,
                                           WatchFolderWatcherService watchFolderWatcherService) {
    return new WatchFolderInitializer(watchFolderDao, watchFolderScanner, watchFolderWatcherService);
  }

}
