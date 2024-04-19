package io.playqd.persistence;

import io.playqd.service.spotify.SpotifyRefreshToken;

import java.util.Optional;

public interface SpotifyRefreshTokenDao {

  Optional<SpotifyRefreshToken> get();

  SpotifyRefreshToken create(String refreshToken);

  void updateRefreshToken(long id, String refreshToken);


}
