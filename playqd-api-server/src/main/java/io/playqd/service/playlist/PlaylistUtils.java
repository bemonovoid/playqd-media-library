package io.playqd.service.playlist;

import io.playqd.commons.data.Playlist;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
public final class PlaylistUtils {

  public static final String PLAYLIST_TITLE_PREFIX = "sync_";

  public static List<Path> listFiles(Playlist playlist) {

    return switch (playlist.format()) {
      case m3u -> listFilesFromM3UPlaylist(playlist);
      case m3u8 -> listFilesFromM3U8Playlist(playlist);
      case spotify -> null;
      case unknown -> null;
    };
  }

  private static List<Path> listFilesFromM3UPlaylist(Playlist playlist) {
    try (Stream<String> lines = Files.lines(playlist.location())) {
      return lines
          .map(PlaylistUtils::getValidPath)
          .toList();
    } catch (IOException e) {
      log.error("Unable to process playlist with id: %s.", e);
      return Collections.emptyList();
    }
  }

  private static List<Path> listFilesFromM3U8Playlist(Playlist playlist) {
    try (Stream<String> lines = Files.lines(playlist.location())) {
      return lines
          // Some players(winamp) prepend '\uFEFF' 65279 to the first line: '#EXT'
          .filter(line -> line.charAt(0) != '#' && line.charAt(0) != '\uFEFF')
          .map(PlaylistUtils::getValidPath)
          .filter(Objects::nonNull)
          .toList();
    } catch (IOException e) {
      log.error("Unable to process playlist with id: %s.", e);
      return Collections.emptyList();
    }
  }

  private static Path getValidPath(String line) {
    try {
      return Paths.get(line);
    } catch (Exception e) {
      log.warn("Path wasn't a valid file.", e);
      return null;
    }
  }
}