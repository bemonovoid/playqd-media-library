package io.playqd.config.lifecycle;

import io.playqd.config.properties.PlayqdProperties;
import io.playqd.service.playlist.PlaylistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;

@Slf4j
@Component
class PlaylistsRefreshApplicationRunner implements ApplicationRunner {

  private final PlaylistService playlistService;
  private final PlayqdProperties playqdProperties;

  PlaylistsRefreshApplicationRunner(PlaylistService playlistService, PlayqdProperties playqdProperties) {
    this.playlistService = playlistService;
    this.playqdProperties = playqdProperties;
  }

  @Override
  public void run(ApplicationArguments args) {
    playlistService.refreshPlaylists();
//    createPlaylistsDir();
  }

  private void createPlaylistsDir() {
    var workingDir = playqdProperties.getWorkingDirOrDefault();
    try {
      if (!Files.isDirectory(workingDir)) {
        throw new IllegalStateException(String.format("Working dir is not a directory. %s", workingDir));
      }
      var playlistsDir = playqdProperties.getPlaylistsDir();
      if (!Files.exists(playlistsDir)) {
        Files.createDirectory(playlistsDir);
        log.info("Playlists directory was successfully created.");
      }
      log.info("Playlists directory: {}", playlistsDir);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create playlists dir.", e);
    }
  }
}