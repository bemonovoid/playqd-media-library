package io.playqd.service.playlist;

import io.playqd.config.properties.PlayqdProperties;
import io.playqd.exception.PlayqdException;
import io.playqd.model.AudioFile;
import io.playqd.model.M3u8PlaylistFile;
import io.playqd.model.PlaylistFile;
import io.playqd.persistence.AudioFileDao;
import io.playqd.util.FileUtils;
import io.playqd.util.UUIDV3Ids;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

  private static final Set<String> SUPPORTED_FORMATS = Set.of("m3u8");

  private final Map<String, PlaylistFilePath> playlistFileRefs = new HashMap<>();

  private final Path playlistsDir;
  private final AudioFileDao audioFileDao;

  public PlaylistServiceImpl(PlayqdProperties playqdProperties, AudioFileDao audioFileDao) {
    this.audioFileDao = audioFileDao;
    this.playlistsDir = playqdProperties.getPlaylistsDir();
  }

  private static PlaylistFile createPlaylistFromFile(Path path) {
    var fileNameExtension = FileUtils.getFileNameAndExtension(path.getFileName().toString());
    return new M3u8PlaylistFile(fileNameExtension.left(), fileNameExtension.left(), path, countPlaylistItems(path));
  }

  private static long countPlaylistItems(Path path) {
    try (Stream<String> lines = Files.lines(path)) {
      return lines
          .filter(line -> !line.startsWith("#"))
          .filter(line -> getValidPath(line) != null)
          .count();
    } catch (IOException e) {
      log.error("Count failed.", e);
      return 0;
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

  public PlaylistFile getPlaylistFile(String playlistId) {
    var playlistFileRef = playlistFileRefs.computeIfAbsent(playlistId, key -> findPlaylistFileRef(key).orElse(null));
    if (playlistFileRef == null) {
      throw new PlayqdException("Not found");
    }
    var playlistLocation = playlistFileRef.location();
    var nameAndFormat = FileUtils.getFileNameAndExtension(playlistLocation.toString());
    return new M3u8PlaylistFile(
        nameAndFormat.left(), nameAndFormat.right(), playlistLocation, countPlaylistItems(playlistLocation));
  }

  @Override
  public List<AudioFile> getPlaylistAudioFiles(String playlistId) {
    var playlistFile = getPlaylistFile(playlistId);
    try (Stream<String> lines = Files.lines(playlistFile.location())) {
      var fileLocations = lines
          .filter(line -> !line.startsWith("#"))
          .map(PlaylistServiceImpl::getValidPath)
          .filter(Objects::nonNull)
          .map(Path::toString)
          .toList();
      return audioFileDao.getAudioFilesByLocationIn(fileLocations).getContent();
    } catch (IOException e) {
      log.error("", e);
      return Collections.emptyList();
    }
  }

  private Optional<PlaylistFilePath> findPlaylistFileRef(String playlistId) {
    try (Stream<Path> files = Files.list(playlistsDir)) {
      return files
          .filter(path -> SUPPORTED_FORMATS.contains(FileUtils.getFileExtension(path)))
          .filter(path -> UUIDV3Ids.create(path.toString()).equals(playlistId))
          .map(PlaylistFilePath::new)
          .findFirst();
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  private record PlaylistFilePath(Path location) {

  }
}
