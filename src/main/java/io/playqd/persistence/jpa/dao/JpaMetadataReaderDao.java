package io.playqd.persistence.jpa.dao;

import io.playqd.commons.data.Album;
import io.playqd.commons.data.Artist;
import io.playqd.commons.data.Genre;
import io.playqd.model.MediaItemFilter;
import io.playqd.model.MediaItemType;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.MetadataReaderDao;
import io.playqd.persistence.jpa.entity.view.AlbumViewEntity;
import io.playqd.persistence.jpa.entity.view.ArtistViewEntity;
import io.playqd.persistence.jpa.entity.view.GenreViewEntity;
import io.playqd.persistence.jpa.repository.AlbumViewRepository;
import io.playqd.persistence.jpa.repository.ArtistViewRepository;
import io.playqd.persistence.jpa.repository.GenreViewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class JpaMetadataReaderDao implements MetadataReaderDao {

  private final ArtistViewRepository artistViewRepository;
  private final AlbumViewRepository albumViewRepository;
  private final GenreViewRepository genreViewRepository;
  private final AudioFileDao audioFileDao;

  JpaMetadataReaderDao(ArtistViewRepository artistViewRepository,
                       AlbumViewRepository albumViewRepository,
                       GenreViewRepository genreViewRepository,
                       AudioFileDao audioFileDao) {
    this.artistViewRepository = artistViewRepository;
    this.albumViewRepository = albumViewRepository;
    this.genreViewRepository = genreViewRepository;
    this.audioFileDao = audioFileDao;
  }

  @Override
  public long count(MediaItemType mediaItemType, MediaItemFilter filter) {
    switch (mediaItemType) {
      case ARTIST -> {
         return artistViewRepository.count();
      }
      case GENRE -> {
        return genreViewRepository.count();
      }
      case TRACK -> {
        if (filter == null) {
          return 0;
        }
        switch (filter) {
          case liked -> {
            return 0;
          }
          case played -> {
            return audioFileDao.countPlayed();
          }
          case recentlyAdded -> {
            return audioFileDao.countRecentlyAdded();
          }
        }
      }
      default -> {
        return 0;
      }
    }
    return 0;
  }

  @Override
  public Page<Artist> getArtists(Pageable pageable) {
    return artistViewRepository.findAll(pageable).map(JpaMetadataReaderDao::fromEntity);
  }

  @Override
  public Page<Album> getAlbums(Pageable pageable) {
    return albumViewRepository.findAll(pageable).map(JpaMetadataReaderDao::fromEntity);
  }

  @Override
  public Page<Album> getAlbumsByArtistId(String artistId, Pageable pageable) {
    return albumViewRepository.findAllByArtistId(artistId, pageable).map(JpaMetadataReaderDao::fromEntity);
  }

  @Override
  public Page<Album> getAlbumsByGenreId(String genreId, Pageable pageable) {
    return albumViewRepository.findAllByGenreId(genreId, pageable).map(JpaMetadataReaderDao::fromEntity);
  }

  @Override
  public Page<Genre> getGenres(Pageable pageable) {
    return genreViewRepository.findAll(pageable).map(JpaMetadataReaderDao::fromEntity);
  }

  private static Artist fromEntity(ArtistViewEntity entity) {
    return new Artist(
        entity.getId(),
        entity.getName(),
        entity.getTotalAlbums(),
        entity.getTotalTracks());
  }

  private static Album fromEntity(AlbumViewEntity entity) {
      return new Album(entity.getId(),
          entity.getName(),
          entity.getReleaseDate(),
          entity.getGenreId(),
          entity.getGenre(),
          entity.getArtistId(),
          entity.getArtistName(),
          entity.isArtworkEmbedded(),
          null,
          entity.getTotalTracks());
  }

  private static Genre fromEntity(GenreViewEntity entity) {
    return new Genre(
        entity.getId(),
        entity.getName(),
        entity.getTotalArtists(),
        entity.getTotalAlbums(),
        entity.getTotalTracks());
  }

}
