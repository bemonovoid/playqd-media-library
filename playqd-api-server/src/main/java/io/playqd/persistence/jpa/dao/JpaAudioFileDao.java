package io.playqd.persistence.jpa.dao;

import io.playqd.commons.data.Album;
import io.playqd.commons.data.Artist;
import io.playqd.commons.data.Playlist;
import io.playqd.commons.utils.Tuple;
import io.playqd.model.AudioFile;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.jpa.entity.AudioFileJpaEntity;
import io.playqd.persistence.jpa.entity.PersistableAuditableEntity;
import io.playqd.persistence.jpa.repository.AudioFileRepository;
import io.playqd.persistence.projection.AlbumsProjection;
import io.playqd.service.playlist.PlaylistUtils;
import io.playqd.service.spotify.SpotifyIds;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class JpaAudioFileDao implements AudioFileDao {

  private final JdbcTemplate jdbcTemplate;
  private final AudioFileRepository audioFileRepository;

  private final SimpleJdbcInsert simpleJdbcInsert;

  public JpaAudioFileDao(JdbcTemplate jdbcTemplate, AudioFileRepository audioFileRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.audioFileRepository = audioFileRepository;
    this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
        .withTableName(AudioFileJpaEntity.TABLE_NAME)
        .usingGeneratedKeyColumns(PersistableAuditableEntity.COL_PK_ID);
  }

  @Override
  public boolean isEmpty() {
    return audioFileRepository.count() == 0;
  }

  @Override
  public long count() {
    return audioFileRepository.count();
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
  public AudioFile getAudioFileByTrackId(String trackId) {
    return audioFileRepository.findByTrackId(trackId);
  }

  @Override
  public <T> T getAudioFileDataByTrackId(String trackId, Class<T> type) {
    return audioFileRepository.findByTrackId(trackId, type);
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
  public Page<AudioFile> getAudioFilesByLocationIn(List<String> locations,
                                                   boolean sortByLocationsOrder,
                                                   Pageable pageable) {
    var sort = Sort.by(AudioFileJpaEntity.FLD_FILE_NAME);
    var pageQuery = PageRequest.of(0, Integer.MAX_VALUE, sort);
    var result = audioFileRepository.findAllByLocationIn(locations, pageQuery);
    if (sortByLocationsOrder) {
      result = sortByLocationsOrder(locations, result);
    }
    return result.map(entity -> entity);
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
  public Page<AudioFile> getAudioFilesWithRating(Pageable page) {
    return audioFileRepository.findByRatingIsNotNull(page).map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getPlayedAudioFiles(Pageable pageable) {
    var queryPageable = pageable;
    var sort = pageable.getSort();
    if (pageable.getSort().isUnsorted()) {
      sort = Sort.by(AudioFileJpaEntity.FLD_FILE_LAST_PLAYBACK_DATE).descending();
      queryPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
    return audioFileRepository.findByFileLastPlaybackDateIsNotNull(queryPageable).map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getAudioFilesAddedToWatchFolderAfterDate(LocalDate afterDate, Pageable pageable) {
    return audioFileRepository.findByFileAddedToWatchFolderDateAfter(afterDate, pageable).map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getAudioFilesByLocationStartsWith(Path path, Pageable pageable) {
    var result = audioFileRepository.findByLocationIsStartingWith(path.toString(), pageable);
    return result.map(entity -> entity);
  }

  @Override
  public Page<AudioFile> getAudioFilesFromPlaylist(Playlist playlist, Pageable pageable) {
    var locations = PlaylistUtils.listFiles(playlist);
    return getAudioFilesByLocationIn(locations.stream().map(Path::toString).toList(), pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public <T> Stream<T> streamByLocationStartsWith(Path basePath, Class<T> type) {
    return audioFileRepository.findByLocationIsStartingWith(basePath.toString(), type);
  }

  @Override
  public <T> Page<T> getDataByLocationStartsWith(Path path, Class<T> type, Pageable pageable) {
    return audioFileRepository.findByLocationIsStartingWith(path.toString(), type, pageable);
  }

  @Override
  public int insertOne(Map<String, Object> audioFileData) {
    return simpleJdbcInsert.execute(audioFileData);
  }

  public int updateOne(Map<String, Object> audioFilesData) {
    return 1;
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
  public void updateAudioFileLastPlaybackDate(long audioFileId, Instant instant) {
    var audioFile = getAudioFile(audioFileId);
    audioFileRepository.updateLastPlaybackDateTime(audioFileId, instant);
    audioFileRepository.updatePlaybackCount(audioFileId, audioFile.playbackCount() + 1);
  }

  @Override
  @Transactional
  public void updateSpotifyIds(AudioFile audioFile, SpotifyIds spotifyIds) {
    if (StringUtils.hasText(spotifyIds.artistId())) {
      audioFileRepository.updateSpotifyArtistId(audioFile.artistId(), spotifyIds.artistId());
    }
    if (StringUtils.hasText(spotifyIds.albumId())) {
      audioFileRepository.updateSpotifyAlbumId(audioFile.albumId(), spotifyIds.albumId());
    }
    if (StringUtils.hasText(spotifyIds.trackId())) {
      audioFileRepository.updateSpotifyTrackId(audioFile.id(), spotifyIds.trackId());
    }
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
  public void updateRating(Long id, String rating) {
    audioFileRepository.updateRating(id, rating);
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

  private static Page<AudioFileJpaEntity> sortByLocationsOrder(List<String> locations,
                                                               Page<AudioFileJpaEntity> audioFiles) {
    Map<String, Integer> locationsOrder = IntStream.range(0, locations.size())
        .boxed()
        .collect(Collectors.toMap(locations::get, i -> i));

    var result = audioFiles.getContent().stream()
        .map(e -> Tuple.from(locationsOrder.get(e.location()), e))
        .sorted(Comparator.comparing(Tuple::left))
        .map(Tuple::right)
        .toList();

    return new PageImpl<>(result, audioFiles.getPageable(), result.size());
  }
}
