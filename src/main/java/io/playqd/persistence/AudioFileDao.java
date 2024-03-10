package io.playqd.persistence;

import io.playqd.commons.data.Album;
import io.playqd.commons.data.Artist;
import io.playqd.model.AudioFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.Path;
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

  AudioFile getFirstAudioFileByAlbumId(String albumId);

  Page<AudioFile> getAudioFiles(Pageable pageable);

  Page<AudioFile> getAudioFilesByArtistId(String artistId, Pageable pageable);

  Page<AudioFile> getAudioFilesByAlbumId(String albumId, Pageable pageable);

  Page<AudioFile> getAudioFilesByGenreId(String genreId, Pageable pageable);

  Page<AudioFile> getAudioFilesByTitle(String title, Pageable page);

  Page<AudioFile> getAudioFilesByPlayed(boolean played, Pageable pageable);

  Page<AudioFile> getAudioFilesAddedToWatchFolderAfterDate(LocalDate afterDate, Pageable pageable);

  Page<AudioFile> getAudioFilesByLocationIn(List<String> locations, boolean sortByLocationsOrder, Pageable pageable);

  Page<AudioFile> getAudioFilesByLocationStartsWith(Path path, Pageable pageable);

  Page<Artist> getGenreArtists(String genreId, Pageable pageable);

  List<Album> getAlbumsAddedAfterDate(LocalDate afterDate);

  <T> Stream<T> streamByLocationStartsWith(Path basePath, Class<T> type);

  int insertAll(List<Map<String, Object>> audioFilesData);

  int updateAll(Map<Long, Map<String, Object>> audioFilesData);

  void updateAudioFileLastPlaybackDate(long audioFileId);

  long deleteAllByIds(List<Long> ids);

  long deleteAllByLocationsStartsWith(Path path);
}
