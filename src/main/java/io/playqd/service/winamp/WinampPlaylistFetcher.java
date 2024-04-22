package io.playqd.service.winamp;

import io.playqd.commons.data.Playlist;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.service.playlist.PlaylistFolderFetcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "playqd.winamp", name = "enabled", havingValue = "true")
final class WinampPlaylistFetcher extends PlaylistFolderFetcher {

  private final Path playlistsDir;

  WinampPlaylistFetcher(PlayqdProperties playqdProperties) {
    this.playlistsDir = Paths.get(playqdProperties.getWinamp().getDir(), "playlists");
    if (!Files.exists(playlistsDir)) {
      throw new IllegalStateException(String.format("'%s' does not exist.", playlistsDir));
    }
  }

  @Override
  public List<Playlist> fetch() {
    return findPlaylistInFolder(playlistsDir);
  }

  @Override
  protected String playlistOrigin() {
    return "winamp";
  }

//  private List<PlaylistElement> getWinampPlaylistsElements() throws IOException {
//    var xmlMapper = new XmlMapper();
//    var winampPlaylists = xmlMapper.readValue(playlistsXmlFile.toFile(), PlaylistsXmlModel.class);
//    if (winampPlaylists.getPlaylists() == 0) {
//      return Collections.emptyList();
//    }
//
//    return winampPlaylists.getPlaylistElements();
//  }

//  private Playlist toPlaylist(PlaylistElement playlistElement) {
//    var playlistFilePath = playlistsDir.resolve(playlistElement.getFilename());
//    var exists = Files.exists(playlistFilePath);
//    return new Playlist(
//        -1,
//        UUIDV3Ids.create(playlistElement.getFilename()),
//        playlistElement.getTitle(),
//        playlistElement.getFilename(),
//        playlistFilePath,
//        getPlaylistLastModifiedDate(playlistFilePath)
//    );
//  }

//  private static LocalDateTime getPlaylistLastModifiedDate(Path path) {
//    try {
//      var fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
//      return FileUtils.getLastModifiedDate(fileAttributes.lastModifiedTime());
//    } catch (IOException e) {
//      log.error("", e);
//      return null;
//    }
//  }
}
