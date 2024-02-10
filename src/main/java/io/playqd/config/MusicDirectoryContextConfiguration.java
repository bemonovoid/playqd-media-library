package io.playqd.config;

import io.playqd.config.lifecycle.ApplicationRunnerOrder;
import io.playqd.config.lifecycle.MusicDirectoryInitializer;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.MusicDirectoryDao;
import io.playqd.persistence.simple.MusicDirectoryDaoImpl;
import io.playqd.service.MusicDirectoryPathResolver;
import io.playqd.service.mediasource.MusicDirectoryManager;
import io.playqd.service.mediasource.MusicDirectoryManagerImpl;
import io.playqd.service.mediasource.MusicDirectoryScanner;
import io.playqd.service.mediasource.MusicDirectoryScannerImpl;
import io.playqd.service.mediasource.MusicDirectoryWatchService;
import io.playqd.service.mediasource.MusicDirectoryWatchServiceImpl;
import io.playqd.service.metadata.FileAttributesToSqlParamsMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class MusicDirectoryContextConfiguration {

  @Bean
  MusicDirectoryDao mediaSourceDao() {
    return new MusicDirectoryDaoImpl();
  }

  @Bean
  MusicDirectoryManager mediaSourceService(MusicDirectoryDao musicDirectoryDao) {
    return new MusicDirectoryManagerImpl(musicDirectoryDao);
  }

  @Bean
  MusicDirectoryScanner mediaSourceScanner(AudioFileDao audioFileDao,
                                           MusicDirectoryDao musicDirectoryDao,
                                           ApplicationEventPublisher eventPublisher,
                                           MusicDirectoryPathResolver musicDirectoryPathResolver,
                                           FileAttributesToSqlParamsMapper fileAttributesToSqlParamsMapper) {
    return new MusicDirectoryScannerImpl(
        audioFileDao, musicDirectoryDao, eventPublisher, musicDirectoryPathResolver, fileAttributesToSqlParamsMapper);
  }

  @Bean
  MusicDirectoryWatchService musicDirectoryWatchService(ApplicationEventPublisher eventPublisher) {
    return new MusicDirectoryWatchServiceImpl(eventPublisher);
  }

  @Bean
  @Order(ApplicationRunnerOrder.MEDIA_SOURCE_INITIALIZER)
  ApplicationRunner musicDirectoryInitializer(PlayqdProperties playqdProperties,
                                              MusicDirectoryManager musicDirectoryManager,
                                              MusicDirectoryScanner musicDirectoryScanner,
                                              MusicDirectoryWatchService musicDirectoryWatchService) {
    return new MusicDirectoryInitializer(
        playqdProperties, musicDirectoryManager, musicDirectoryScanner, musicDirectoryWatchService);
  }

}
