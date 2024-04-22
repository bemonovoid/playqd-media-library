package io.playqd.service.playlist;

import io.playqd.commons.data.Playlist;
import io.playqd.commons.data.PlaylistFormat;
import io.playqd.commons.utils.Tuple;
import io.playqd.service.playlist.PlaylistFetcher;
import io.playqd.service.playlist.PlaylistUtils;
import io.playqd.util.FileUtils;
import io.playqd.util.UUIDV3Ids;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public abstract class PlaylistFolderFetcher implements PlaylistFetcher {

  private static final EnumSet<PlaylistFormat> SUPPORTED_PLAYLISTS_FORMATS = EnumSet.of(
      PlaylistFormat.m3u, PlaylistFormat.m3u8);

  protected List<Playlist> findPlaylistInFolder(Path dir) {
    try (Stream<Path> file = Files.list(dir)) {
      return file
          .map(p -> Tuple.from(PlaylistFormat.fromString(FileUtils.getFileExtension(p)), p))
          .filter(t -> SUPPORTED_PLAYLISTS_FORMATS.contains(t.left()))
          .map(t -> new Playlist(
              -1,
              t.left(),
              UUIDV3Ids.create(t.right().getFileName().toString()),
              buildPlaylistTitle(t.right()),
              t.right().getFileName().toString(),
              t.right(),
              FileUtils.getLastModifiedDate(t.right())
          ))
          .toList();
    } catch (IOException e) {
      log.error("Error when listing playlist files in folder {}", dir, e);
      return Collections.emptyList();
    }
  }

  private String buildPlaylistTitle(Path p) {
    var fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(p.getFileName().toString());
    return String.format("%s%s:%s", PlaylistUtils.PLAYLIST_TITLE_PREFIX, playlistOrigin(), fileNameWithoutExtension);
  }

  protected abstract String playlistOrigin();
}