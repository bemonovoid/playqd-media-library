package io.playqd.persistence;

import io.playqd.commons.data.Album;
import io.playqd.commons.data.Artist;
import io.playqd.commons.data.Playlist;
import io.playqd.model.AudioFile;
import io.playqd.service.spotify.SpotifyIds;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface AudioFileDao {

  boolean isEmpty();

  long count();

  long countPlayed();

  long countNotPlayed();

  long countByAddedToWatchFolderSinceDate(LocalDate sinceDate);

  LocalDate findLatestAddedToWatchFolderDate();

  AudioFile getAudioFile(long id);

  AudioFile getAudioFileByTrackId(String trackId);

  <T> T getAudioFileDataByTrackId(String trackId, Class<T> type);

  AudioFile getFirstAudioFileByAlbumId(String albumId);

  Page<AudioFile> getAudioFiles(Pageable pageable);

  Page<AudioFile> getAudioFilesByArtistId(String artistId, Pageable pageable);

  Page<AudioFile> getAudioFilesByAlbumId(String albumId, Pageable pageable);

  Page<AudioFile> getAudioFilesByGenreId(String genreId, Pageable pageable);

  Page<AudioFile> getAudioFilesByTitle(String title, Pageable page);

  Page<AudioFile> getAudioFilesWithRating(Pageable page);

  Page<AudioFile> getPlayedAudioFiles(Pageable pageable);

  Page<AudioFile> getAudioFilesAddedToWatchFolderAfterDate(LocalDate afterDate, Pageable pageable);

  default Page<AudioFile> getAudioFilesByLocationIn(List<String> locations) {
    return getAudioFilesByLocationIn(locations, Pageable.unpaged());
  }

  default Page<AudioFile> getAudioFilesByLocationIn(List<String> locations, Pageable pageable) {
    return getAudioFilesByLocationIn(locations, false, pageable);
  }

  Page<AudioFile> getAudioFilesByLocationIn(List<String> locations, boolean sortByLocationsOrder, Pageable pageable);

  Page<AudioFile> getAudioFilesByLocationStartsWith(Path path, Pageable pageable);

  Page<AudioFile> getAudioFilesFromPlaylist(Playlist playlist, Pageable pageable);

  Page<Artist> getGenreArtists(String genreId, Pageable pageable);

  List<Album> getAlbumsAddedAfterDate(LocalDate afterDate);

  <T> Stream<T> streamByLocationStartsWith(Path basePath, Class<T> type);

  default <T> Page<T> getDataByLocationStartsWith(Path path, Class<T> type) {
    return getDataByLocationStartsWith(path, type, Pageable.unpaged());
  }

  <T> Page<T> getDataByLocationStartsWith(Path path, Class<T> type, Pageable pageable);

  int insertOne(Map<String, Object> audioFileData);

  int insertAll(List<Map<String, Object>> audioFilesData);

  int updateAll(Map<Long, Map<String, Object>> audioFilesData);

  void updateRating(Long id, String rating);

  void updateAudioFileLastPlaybackDate(long audioFileId);

  void updateAudioFileLastPlaybackDate(long audioFileId, Instant instant);

  void updateSpotifyIds(AudioFile audioFile, SpotifyIds spotifyIds);

  long deleteAllByIds(List<Long> ids);

  long deleteAllByLocationsStartsWith(Path path);
}
