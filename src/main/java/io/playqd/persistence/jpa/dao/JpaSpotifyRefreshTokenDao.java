package io.playqd.persistence.jpa.dao;

import io.playqd.persistence.SpotifyRefreshTokenDao;
import io.playqd.persistence.jpa.entity.SpotifyRefreshTokenEntity;
import io.playqd.persistence.jpa.repository.SpotifyCredentialsRepository;
import io.playqd.service.spotify.SpotifyRefreshToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class JpaSpotifyRefreshTokenDao implements SpotifyRefreshTokenDao {

  private final SpotifyCredentialsRepository repository;

  public JpaSpotifyRefreshTokenDao(SpotifyCredentialsRepository repository) {
    this.repository = repository;
  }

  @Override
  public Optional<SpotifyRefreshToken> get() {
    var all = repository.findAll();
    if (all.size() != 1) {
      return Optional.empty();
    }
    var entity = all.getFirst();

    return Optional.of(new SpotifyRefreshToken(entity.getId(), entity.getRefreshToken()));
  }

  @Override
  @Transactional
  public SpotifyRefreshToken create(String refreshToken) {
    var entity = new SpotifyRefreshTokenEntity();
    entity.setRefreshToken(refreshToken);
    entity = repository.saveAndFlush(entity);
    return new SpotifyRefreshToken(entity.getId(), entity.getRefreshToken());
  }

  @Override
  @Transactional
  public void updateRefreshToken(long id, String refreshToken) {
    var entity = repository.get(id);
    entity.setRefreshToken(refreshToken);
    repository.saveAndFlush(entity);
  }
}
