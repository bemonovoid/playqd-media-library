package io.playqd.service.musicbee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.playqd.commons.data.Playlist;
import io.playqd.config.properties.MusicBeeProperties;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.exception.PlaylistServiceException;
import io.playqd.service.playlist.PlaylistFolderFetcher;
import io.playqd.util.FileUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "playqd.musicbee", name = "enabled", havingValue = "true")
final class MusicBeePlaylistFetcher extends PlaylistFolderFetcher {

  private final MusicBeeProperties musicBeeProperties;

  MusicBeePlaylistFetcher(PlayqdProperties playqdProperties) {
    this.musicBeeProperties = playqdProperties.getMusicbee();
  }

  @Override
  public List<Playlist> fetch() {
    var playlists = findPlaylistInFolder(Paths.get(musicBeeProperties.getPlaylistsDir()));
    var exportedPlaylists = findPlaylistInFolder(Paths.get(musicBeeProperties.getExportedPlaylistsDir()));
    return Stream.concat(playlists.stream(), exportedPlaylists.stream()).toList();
  }

  @Override
  protected String playlistOrigin() {
    return "musicbee";
  }

  private List<String> getAutoPlaylists(Path dir) {

//    var winampPlaylists = xmlMapper.readValue(playlistsXmlFile.toFile(), PlaylistsXmlModel.class);
    try (Stream<Path> path = Files.list(dir)) {
      path
          .filter(p -> FileUtils.getFileExtension(p).equals("xautopf"))
          .map(this::read)
          .toList();
      return null;
    } catch (Exception e) {
      log.error("Error when listing playlist files in folder {}", dir, e);
      return Collections.emptyList();
    }
  }

  private AutoPlaylistXmlModel read(Path path) {
    try {
      var xmlMapper = new XmlMapper();
      return xmlMapper.readValue(path.toFile(), AutoPlaylistXmlModel.class);
    } catch (Exception e) {
      throw new PlaylistServiceException(e);
    }
  }

  @Getter
  @Setter(AccessLevel.PACKAGE)
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class AutoPlaylistXmlModel {

    @JacksonXmlProperty(localName = "SmartPlaylist")
    private SmartPlaylist smartPlaylist;

  }

  @Getter
  @Setter(AccessLevel.PACKAGE)
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class SmartPlaylist {

    @JacksonXmlProperty(localName = "Source")
    private Source source;

  }

  @Getter
  @Setter(AccessLevel.PACKAGE)
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Source {

    @JacksonXmlProperty(localName = "Conditions")
    private Conditions conditions;

  }

  @Getter
  @Setter(AccessLevel.PACKAGE)
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Conditions {

    @JacksonXmlProperty(localName = "Condition")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Condition> conditions;
  }

  @Getter
  @Setter(AccessLevel.PACKAGE)
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Condition {

    @JacksonXmlProperty(localName = "Filed")
    private String filed;

    @JacksonXmlProperty(localName = "Comparison")
    private String comparison;

    @JacksonXmlProperty(localName = "Value")
    private String value;
  }
}