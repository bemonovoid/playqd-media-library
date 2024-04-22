package io.playqd.persistence.jpa.dao;

import io.playqd.commons.data.Genre;
import io.playqd.persistence.GenreDao;
import io.playqd.persistence.jpa.entity.view.GenreViewEntity;
import io.playqd.persistence.jpa.repository.GenreViewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
final class JpaGenreViewDao implements GenreDao {

  private final GenreViewRepository repository;

  JpaGenreViewDao(GenreViewRepository repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public Page<Genre> getAll(Pageable pageable) {
    return repository.findAll(pageable).map(JpaGenreViewDao::fromEntity);
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
