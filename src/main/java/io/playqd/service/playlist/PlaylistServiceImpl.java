package io.playqd.service.playlist;

import io.playqd.commons.data.Playlist;
import io.playqd.commons.data.PlaylistFormat;
import io.playqd.service.MusicDirectoryPathResolver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

  private static final Map<String, Playlist> PLAYLIST_FILES_CACHE = new HashMap<>();
  private static final EnumSet<PlaylistFormat> SUPPORTED_FORMATS = EnumSet.of(PlaylistFormat.m3u8);

  private final MusicDirectoryPathResolver musicDirectoryPathResolver;
  private final Set<PlaylistFilesFetcher> playlistFilesFetchers;

  public PlaylistServiceImpl(MusicDirectoryPathResolver musicDirectoryPathResolver,
                             Set<PlaylistFilesFetcher> playlistFilesFetchers) {
    this.musicDirectoryPathResolver = musicDirectoryPathResolver;
    this.playlistFilesFetchers = playlistFilesFetchers;
  }

  @Override
  public long count() {
    return playlistFilesFetchers.stream().mapToLong(PlaylistFilesFetcher::count).sum();
  }

  @Override
  public List<Playlist> getPlaylists() {
    return playlistFilesFetchers.stream()
        .flatMap(playlistFilesFetcher -> playlistFilesFetcher.fetch().stream())
        .peek(playlistFile -> PLAYLIST_FILES_CACHE.putIfAbsent(playlistFile.id(), playlistFile))
        .toList();
  }

  @Override
  public List<String> playlistFiles(String playlistId) {
    if (PLAYLIST_FILES_CACHE.isEmpty()) {
      getPlaylists();
    }
    if (!PLAYLIST_FILES_CACHE.containsKey(playlistId)) {
      log.warn("Playlist with id: '{}' was not found or does not exist.", playlistId);
      return Collections.emptyList();
    }
    var playlistFile = PLAYLIST_FILES_CACHE.get(playlistId);

    if (!Files.exists(playlistFile.location())) {
      log.error("File '{}' for playlist with id: '{}' does not exist.", playlistFile.location(), playlistId);
      return Collections.emptyList();
    }

    if (!SUPPORTED_FORMATS.contains(playlistFile.format())) {
      log.error("Unsupported playlist format: '{}' for playlist with id: '{}'.", playlistFile.format(), playlistId);
      return Collections.emptyList();
    }

    try (Stream<String> lines = Files.lines(playlistFile.location())) {
      return lines
          // Some players(winamp) prepend '\uFEFF' 65279 to the first line: '#EXT'
          .filter(line -> line.charAt(0) != '#' && line.charAt(0) != '\uFEFF')
          .map(PlaylistServiceImpl::getValidPath)
          .filter(Objects::nonNull)
          .map(musicDirectoryPathResolver::relativize)
          .filter(Objects::nonNull)
          .map(Path::toString)
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