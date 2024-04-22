package io.playqd.persistence.jpa.dao;

import io.playqd.commons.data.Artist;
import io.playqd.persistence.ArtistDao;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.jpa.entity.view.ArtistViewEntity;
import io.playqd.persistence.jpa.repository.ArtistViewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
final class JpaArtistViewDao implements ArtistDao {

  private final AudioFileDao audioFileDao;
  private final ArtistViewRepository repository;

  JpaArtistViewDao(AudioFileDao audioFileDao, ArtistViewRepository repository) {
    this.repository = repository;
    this.audioFileDao = audioFileDao;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public Optional<Artist> getById(String artistId) {
    return repository.findById(artistId).map(JpaArtistViewDao::fromEntity);
  }

  @Override
  public Page<Artist> getAll(Pageable pageable) {
    return repository.findAll(pageable).map(JpaArtistViewDao::fromEntity);
  }

  @Override
  public Page<Artist> getByGenreId(String genreId, Pageable pageable) {
    return audioFileDao.getGenreArtists(genreId, pageable);
  }

  private static Artist fromEntity(ArtistViewEntity entity) {
    return new Artist(
        entity.getId(),
        entity.getName(),
        entity.getTotalAlbums(),
        entity.getTotalTracks());
  }
}
