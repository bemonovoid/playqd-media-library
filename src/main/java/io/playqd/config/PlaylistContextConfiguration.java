package io.playqd.config;

import io.playqd.config.lifecycle.ApplicationRunnerOrder;
import io.playqd.config.lifecycle.PlaylistsDirectoryInitializer;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.service.playlist.PlaylistFilesFetcher;
import io.playqd.service.playlist.PlaylistService;
import io.playqd.service.playlist.PlaylistServiceImpl;
import io.playqd.service.playlist.provider.winamp.WinampPlaylistFilesFetcher;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.Set;

@Configuration
public class PlaylistContextConfiguration {

  @Bean
  @Order(ApplicationRunnerOrder.PLAYLISTS_DIRECTORY_INITIALIZER)
  ApplicationRunner playlistsDirectoryInitializer(PlayqdProperties playqdProperties) {
    return new PlaylistsDirectoryInitializer(playqdProperties);
  }

  @Bean
  @ConditionalOnProperty(prefix = "playqd.playlists.winamp", name = "dir")
  PlaylistFilesFetcher winampPlaylistFilesFetcher(PlayqdProperties playqdProperties) {
    return new WinampPlaylistFilesFetcher(playqdProperties.getPlaylists().getWinamp());
  }

  @Bean
  PlaylistService playlistService(Set<PlaylistFilesFetcher> playlistFilesFetchers) {
    return new PlaylistServiceImpl(playlistFilesFetchers);
  }

}
