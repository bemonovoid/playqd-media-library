package io.playqd.config;

import io.playqd.config.lifecycle.ApplicationRunnerOrder;
import io.playqd.config.lifecycle.PlaylistsDirectoryInitializer;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.persistence.AudioFileDao;
import io.playqd.service.playlist.PlaylistService;
import io.playqd.service.playlist.PlaylistServiceImpl;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class PlaylistContextConfiguration {

  @Bean
  @Order(ApplicationRunnerOrder.PLAYLISTS_DIRECTORY_INITIALIZER)
  ApplicationRunner playlistsDirectoryInitializer(PlayqdProperties playqdProperties) {
    return new PlaylistsDirectoryInitializer(playqdProperties);
  }

  @Bean
  PlaylistService playlistService(PlayqdProperties playqdProperties, AudioFileDao audioFileDao) {
    return new PlaylistServiceImpl(playqdProperties, audioFileDao);
  }

}
