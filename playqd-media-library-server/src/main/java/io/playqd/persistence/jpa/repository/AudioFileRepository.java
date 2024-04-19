package io.playqd.persistence.jpa.repository;

import io.playqd.persistence.jpa.entity.AudioFileJpaEntity;
import io.playqd.persistence.projection.AlbumsProjection;
import io.playqd.persistence.projection.ArtistProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Stream;

public interface AudioFileRepository extends IdentityJpaRepository<AudioFileJpaEntity> {

  long countByFileLastPlaybackDateIsNull();

  long countByFileLastPlaybackDateIsNotNull();

  long countByFileAddedToWatchFolderDateAfter(LocalDate dateAfter);

  @Query("select max(a.fileAddedToWatchFolderDate) from AudioFileJpaEntity a")
  LocalDate findMaxAddedToWatchFolderDate();

  AudioFileJpaEntity findByTrackId(String trackId);

  <T> T findByTrackId(String trackId, Class<T> projection);

  AudioFileJpaEntity findFirstByAlbumId(String albumId);

  Page<AudioFileJpaEntity> findAllByLocationIn(Collection<String> locations, Pageable page);

  Page<AudioFileJpaEntity> findAllByArtistId(String artistId, Pageable page);

  Page<AudioFileJpaEntity> findAllByAlbumId(String albumId, Pageable page);

  Page<AudioFileJpaEntity> findAllByGenreId(String genreId, Pageable page);

  Page<AudioFileJpaEntity> findByFileLastPlaybackDateIsNotNull(Pageable page);

  Page<AudioFileJpaEntity> findByRatingIsNotNull(Pageable page);

  Page<AudioFileJpaEntity> findByFileAddedToWatchFolderDateAfter(LocalDate dateAfter, Pageable page);

  default Page<AudioFileJpaEntity> findByTrackTitleOrFileNameContainingIgnoreCase(String value, Pageable pageable) {
    return findByTrackNameContainingIgnoreCaseOrNameContainingIgnoreCase(value, value, pageable);
  }

  Page<AudioFileJpaEntity> findByTrackNameContainingIgnoreCaseOrNameContainingIgnoreCase(String trackName,
                                                                                         String filename,
                                                                                         Pageable pageable);

  Page<AudioFileJpaEntity> findByLocationIsStartingWith(String path, Pageable pageable);

  @Query("select a.artistId as id, a.artistName as name, " +
      "count(distinct a.albumName) as albums, count(a.id) as tracks " +
      "from AudioFileJpaEntity a where a.genreId = ?1 " +
      "group by a.artistId, a.artistName")
  Page<ArtistProjection> findArtistsByGenreId(String genreId, Pageable pageable);

  @Query("select a.albumId as id, a.albumName as name, a.albumReleaseDate as releaseDate, " +
      "a.fileAddedToWatchFolderDate as addedToWatchFolderDate, a.genreId as genreId, a.genre as genre, " +
      "a.artworkEmbedded as artworkEmbedded, a.artistId as artistId, a.artistName as artistName, " +
      "count(a.id) as tracks from AudioFileJpaEntity a where a.fileAddedToWatchFolderDate > ?1 " +
      "group by a.albumId, a.albumName, a.fileAddedToWatchFolderDate, a.albumReleaseDate, a.genreId, a.genre, a.artworkEmbedded, a.artistId, a.artistName")
  Stream<AlbumsProjection> streamAlbumsAddedAfterDate(LocalDate afterDate, Pageable pageable);

  <T> Stream<T> findByLocationIsStartingWith(String basePath, Class<T> type);

  <T> Page<T> findByLocationIsStartingWith(String path, Class<T> type, Pageable pageable);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update AudioFileJpaEntity a set a.playbackCount = ?2 where a.id = ?1")
  int updatePlaybackCount(long id, int count);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Transactional
  @Query("update AudioFileJpaEntity a set a.fileLastPlaybackDate = ?2 where a.id = ?1")
  int updateLastPlaybackDateTime(long id, Instant fileLastPlaybackDate);

  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update AudioFileJpaEntity a set a.rating = ?2 where a.id = ?1")
  int updateRating(long id, String rating);

  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update AudioFileJpaEntity a set a.spotifyArtistId = ?2 where a.artistId = ?1")
  int updateSpotifyArtistId(String artistId, String spotifyArtistId);

  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update AudioFileJpaEntity a set a.spotifyAlbumId = ?2 where a.albumId = ?1")
  int updateSpotifyAlbumId(String albumId, String spotifyAlbumId);

  @Transactional
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update AudioFileJpaEntity a set a.spotifyTrackId = ?2 where a.id = ?1")
  int updateSpotifyTrackId(long id, String spotifyTrackId);

  @Modifying
  long deleteByIdIsIn(Collection<Long> ids);

  @Modifying
  long deleteByLocationIsStartingWith(String basePath);

  @SuppressWarnings("SqlWithoutWhere")
  @Modifying
  @Query("delete from AudioFileJpaEntity a")
  void deleteAll();

}
