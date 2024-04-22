package io.playqd.persistence.jpa.dao;

import io.playqd.commons.data.Album;
import io.playqd.persistence.AlbumDao;
import io.playqd.persistence.jpa.entity.view.AlbumViewEntity;
import io.playqd.persistence.jpa.repository.AlbumViewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
final class JpaAlbumViewDao implements AlbumDao {

  private final AlbumViewRepository repository;

  JpaAlbumViewDao(AlbumViewRepository repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public Optional<Album> getById(String albumId) {
    return repository.findById(albumId).map(JpaAlbumViewDao::fromEntity);
  }

  @Override
  public Page<Album> getAll(Pageable pageable) {
    return repository.findAll(pageable).map(JpaAlbumViewDao::fromEntity);
  }

  @Override
  public Page<Album> getByArtistId(String artistId, Pageable pageable) {
    return repository.findAllByArtistId(artistId, pageable).map(JpaAlbumViewDao::fromEntity);
  }

  @Override
  public Page<Album> getByGenreId(String genreId, Pageable pageable) {
    return repository.findAllByGenreId(genreId, pageable).map(JpaAlbumViewDao::fromEntity);
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
}
