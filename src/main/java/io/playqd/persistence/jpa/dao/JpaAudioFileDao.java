package io.playqd.persistence.jpa.dao;

import io.playqd.commons.data.Album;
import io.playqd.commons.data.Artist;
import io.playqd.model.AudioFile;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.jpa.entity.AudioFileJpaEntity;
import io.playqd.persistence.jpa.entity.PersistableAuditableEntity;
import io.playqd.persistence.jpa.repository.AudioFileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

@Slf4j
public class JpaAudioFileDao implements AudioFileDao {

  private final JdbcTemplate jdbcTemplate;
  private final AudioFileRepository audioFileRepository;

  public JpaAudioFileDao(JdbcTemplate jdbcTemplate, AudioFileRepository audioFileRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.audioFileRepository = audioFileRepository;
  }

  @Override
  public boolean isEmpty() {
    return audioFileRepository.count() == 0;
  }

  @Override
  public long countPlayed() {
    return audioFileRepository.countByFileLastPlaybackDateIsNotNull();
  }

  @Override
  public long countNotPlayed() {
    return audioFileRepository.countByFileLastPlaybackDateIsNull();
  }

  @Override
  public long countByAddedToWatchFolderSinceDate(LocalDate sinceDate) {
    return audioFileRepository.countByFileAddedToWatchFolderDateAfter(sinceDate);
  }

  @Override
  public LocalDate findLatestAddedToWatchFolderDate() {
    return audioFileRepository.findMaxAddedToWatchFolderDate();
  }

  @Override
  public AudioFile getAudioFile(long id) {
    return audioFileRepository.get(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<Artist> getGenreArtists(String genreId, Pageable pageable) {
    return audioFileRepository.findArtistsByGenreId(genreId, pageable)
        .map(prj -> new Artist(prj.getId(), prj.getName(), prj.getAlbums(), prj.getTracks()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<Album> getAlbumsAddedAfterDate(LocalDate afterDate) {
    var sort = Sort.by(AudioFileJpaEntity.FLD_FILE_ADDED_TO_WATCH_FOLDER_DATE).descending();
    var pageable = PageRequest.of(0, Integer.MAX_VALUE, sort);
    return audioFileRepository
        .streamAlbumsAddedAfterDate(afterDate, pageable).map(JpaAudioFileDao::fromProjection).toList();
  }

  @Override
  public AudioFile getFirstAudioFileByAlbumId(String albumId) {
    return audioFileRepository.findFirstByAlbumId(albumId);
  }

  @Override
  public Page<AudioFile> getAudioFiles(Pageable pageable) {
    return audioFileRepository.findAll(pageable).map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getAudioFilesByLocationIn(Collection<String> locations, Pageable pageable) {
    return audioFileRepository.findAllByLocationIn(locations, pageable).map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getAudioFilesByArtistId(String artistId, Pageable pageable) {
    return audioFileRepository.findAllByArtistId(artistId, pageable).map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getAudioFilesByAlbumId(String albumId, Pageable pageable) {
    return audioFileRepository.findAllByAlbumId(albumId, pageable).map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getAudioFilesByGenreId(String genreId, Pageable pageable) {
    return audioFileRepository.findAllByGenreId(genreId, pageable).map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getAudioFilesByTitle(String title, Pageable page) {
    return audioFileRepository.findByTrackTitleOrFileNameContainingIgnoreCase(title, page).map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getAudioFilesByPlayed(boolean played, Pageable pageable) {
    if (played) {
      var queryPageable = pageable;
      var sort = pageable.getSort();
      if (pageable.getSort().isUnsorted()) {
        sort = Sort.by(AudioFileJpaEntity.FLD_FILE_LAST_PLAYBACK_DATE).descending();
        queryPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
      }
      return audioFileRepository.findByFileLastPlaybackDateIsNotNull(queryPageable).map(entity -> entity);
    } else {
      return audioFileRepository.findByFileLastPlaybackDateIsNull(pageable).map(entity -> entity);
    }
  }

  @Override
  public Page<AudioFile> getAudioFilesAddedToWatchFolderAfterDate(LocalDate afterDate, Pageable pageable) {
    return audioFileRepository.findByFileAddedToWatchFolderDateAfter(afterDate, pageable).map(entity -> entity);
  }

  @Override
  @Transactional(readOnly = true)
  public <T> Stream<T> streamByLocationStartsWith(Path basePath, Class<T> type) {
    return audioFileRepository.findByLocationIsStartingWith(basePath.toString(), type);
  }

  @Override
  public int insertAll(List<Map<String, Object>> audioFilesData) {
    var sqlParameterSources = audioFilesData.stream()
        .map(MapSqlParameterSource::new)
        .toArray(SqlParameterSource[]::new);

    var jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
        .withTableName(AudioFileJpaEntity.TABLE_NAME)
        .usingGeneratedKeyColumns(PersistableAuditableEntity.COL_PK_ID);

    return jdbcInsert.executeBatch(sqlParameterSources).length;
  }

  @Override
  @Transactional
  public void updateAudioFileLastPlaybackDate(long audioFileId) {
    var audioFile = getAudioFile(audioFileId);
    audioFileRepository.updateLastPlaybackDateTime(audioFileId, Instant.now());
    audioFileRepository.updatePlaybackCount(audioFileId, audioFile.playbackCount() + 1);
  }

  @Override
  public int updateAll(Map<Long, Map<String, Object>> audioFilesData) {
    if (CollectionUtils.isEmpty(audioFilesData)) {
      return 0;
    }

    var updatesCount = 0;
    var setters = new StringJoiner(",");

    Map<String, Object> updates = new LinkedHashMap<>(AudioFileJpaEntity.METADATA_UPDATABLE_COLUMNS.size());

    for (Map.Entry<Long, Map<String, Object>> updatedMetadata : audioFilesData.entrySet()) {
      updatedMetadata.getValue().entrySet().stream()
          .filter(e -> AudioFileJpaEntity.METADATA_UPDATABLE_COLUMNS.contains(e.getKey()))
          .forEach(entry -> updates.put(entry.getKey(), entry.getValue()));
      for (Map.Entry<String, ?> entry : updates.entrySet()) {
        setters.add(entry.getKey() + "=?");
      }

      var sql = String.format("UPDATE %s SET %s WHERE %s=?",
          AudioFileJpaEntity.TABLE_NAME, setters, AudioFileJpaEntity.COL_PK_ID);

      jdbcTemplate.update(sql, ps -> {
        var i = 1;
        for (Map.Entry<String, ?> entry : updates.entrySet()) {
          ps.setObject(i, entry.getValue());
          i++;
        }
        ps.setLong(i, updatedMetadata.getKey());
      });

      updatesCount++;

    }
    return updatesCount;
  }

  @Override
  @Transactional
  public long deleteAllByIds(List<Long> ids) {
    return audioFileRepository.deleteByIdIsIn(ids);
  }

  @Override
  @Transactional
  public long deleteAllByLocationsStartsWith(Path path) {
    return audioFileRepository.deleteByLocationIsStartingWith(path.toString());
  }

  private static Album fromProjection(AlbumsProjection projection) {
    return new Album(
        projection.getId(),
        projection.getName(),
        projection.getReleaseDate(),
        projection.getGenreId(),
        projection.getGenre(),
        projection.getArtistId(),
        projection.getArtistName(),
        projection.getArtworkEmbedded(),
        projection.getAddedToWatchFolderDate(),
        projection.getTracks());
  }
}
