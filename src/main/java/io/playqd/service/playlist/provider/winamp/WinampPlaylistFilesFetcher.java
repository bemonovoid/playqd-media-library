package io.playqd.service.playlist.provider.winamp;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.playqd.commons.data.Playlist;
import io.playqd.commons.data.PlaylistFormat;
import io.playqd.commons.data.PlaylistProvider;
import io.playqd.config.properties.WinampPlaylistProperties;
import io.playqd.exception.PlaylistServiceException;
import io.playqd.service.playlist.PlaylistFilesFetcher;
import io.playqd.util.FileUtils;
import io.playqd.util.UUIDV3Ids;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Slf4j
public class WinampPlaylistFilesFetcher implements PlaylistFilesFetcher {

  private final WinampPlaylistProperties winampPlaylistProps;

  public WinampPlaylistFilesFetcher(WinampPlaylistProperties winampPlaylistProps) {
    this.winampPlaylistProps = winampPlaylistProps;
  }

  @Override
  public long count() {
    try {
      return getWinampPlaylistsElements().size();
    } catch (IOException e) {
      log.error("", e);
      return 0;
    }
  }

  @Override
  public List<Playlist> fetch() {
    try {
      return getWinampPlaylistsElements().stream().map(this::toPlaylist).toList();
    } catch (IOException e) {
      throw new PlaylistServiceException(e);
    }
  }

  private List<PlaylistElement> getWinampPlaylistsElements() throws IOException {
    var winampPlaylistsPath = Paths.get(winampPlaylistProps.getDir());
    var winampPlaylistsXmlFile = winampPlaylistsPath.resolve(winampPlaylistProps.getPlaylistsXmlFileName()).toFile();

    if (!winampPlaylistsXmlFile.exists()) {
      throw new PlaylistServiceException(String.format("'%s' does not exist.", winampPlaylistsXmlFile.getPath()));
    }

    if (winampPlaylistsXmlFile.isDirectory()) {
      throw new PlaylistServiceException(
          String.format("'%s' must be xml file but was a directory.", winampPlaylistsXmlFile.getPath()));
    }

    var xmlMapper = new XmlMapper();

    var winampPlaylists = xmlMapper.readValue(winampPlaylistsXmlFile, PlaylistsXmlModel.class);

    if (winampPlaylists.getPlaylists() == 0) {
      return Collections.emptyList();
    }

    return winampPlaylists.getPlaylistElements();
  }

  private Playlist toPlaylist(PlaylistElement playlistElement) {
    return new Playlist(
        UUIDV3Ids.create(playlistElement.getFilename()),
        playlistElement.getTitle(),
        playlistElement.getFilename(),
        PlaylistFormat.fromString(FileUtils.getFileExtension(playlistElement.getFilename())),
        PlaylistProvider.winamp,
        Paths.get(winampPlaylistProps.getDir()).resolve(playlistElement.getFilename()),
        playlistElement.getSongs());
  }
}
