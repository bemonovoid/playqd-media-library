package io.playqd.persistence;

import io.playqd.commons.data.Album;
import io.playqd.commons.data.Artist;
import io.playqd.commons.data.Genre;
import io.playqd.model.AudioFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface AudioFileDao {

  Page<Artist> getArtists(Pageable pageable);

  Page<AudioFile> getAudioFiles(Filter filter, Pageable pageable);

  long countGenres();

  long countArtists();

  long countPlayed();

  long countRecentlyAdded();

  long countAlbumAudioFiles(String albumId);

  long countArtistAudioFiles(String artistId);

  AudioFile getAudioFile(long id);

  AudioFile getFirstAudioFileByAlbumId(String albumId);

  List<Genre> getAllGenres();

  default Page<AudioFile> getRecentlyAdded(Pageable pageable) {
    return getRecentlyAdded(LocalDate.now().minusMonths(3).minusDays(1), pageable);
  }

  Page<AudioFile> getRecentlyAdded(LocalDate dateAfter, Pageable pageable);

  Page<AudioFile> getPlayed(Pageable pageable);

  List<Artist> getGenreArtists(String genreId);

  List<Album> getGenreAlbums(String genreId);

  List<Album> getAlbumsByArtistId(String artistId);

  List<Album> getAlbumsAddedAfterDate(LocalDate afterDate);

  List<AudioFile> getAudioFilesByAlbumId(String albumId);

  List<AudioFile> getAudioFilesByArtistId(String artistId);

  Page<AudioFile> getAudioFilesByLocationIn(Collection<String> locations);

  Page<AudioFile> getAudioFilesAddedAfterDate(LocalDate dateAfter, Pageable pageable);

  <T> Stream<T> streamByLocationStartsWith(Path basePath, Class<T> type);

  void updateAudioFileLastPlaybackDate(long audioFileId);

  void setNewLastRecentlyAddedDate(LocalDateTime lastRecentlyAddedDateTime);

  int insertAll(List<Map<String, Object>> audioFilesData);

  int updateAll(Map<Long, Map<String, Object>> audioFilesData);

  long deleteAllByIds(List<Long> ids);

  long deleteAllByLocationsStartsWith(Path path);
}
