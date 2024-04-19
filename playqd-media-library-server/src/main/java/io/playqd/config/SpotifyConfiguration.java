package io.playqd.config;

import io.playqd.config.properties.PlayqdProperties;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.SpotifyRefreshTokenDao;
import io.playqd.persistence.jpa.dao.JpaSpotifyRefreshTokenDao;
import io.playqd.persistence.jpa.repository.SpotifyCredentialsRepository;
import io.playqd.service.spotify.SpotifyLibrarySynchronizer;
import io.playqd.service.spotify.SpotifyTracksSearch;
import io.playqd.service.spotify.client.SpotifyApiContext;
import io.playqd.service.spotify.client.SpotifyAuthentication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import se.michaelthelin.spotify.SpotifyApi;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "playqd.spotify", name = "enabled", havingValue = "true")
class SpotifyConfiguration {

  @Bean
  SpotifyRefreshTokenDao spotifyCredentialsDao(SpotifyCredentialsRepository repository) {
    return new JpaSpotifyRefreshTokenDao(repository);
  }

  @Bean
  SpotifyApi spotifyApi(PlayqdProperties playqdProperties, SpotifyRefreshTokenDao dao) {
    var spotifyProperties = playqdProperties.getSpotify();
//    var s = SpotifyAuthentication.getAuthorizationCodeUri(spotifyProperties);
//    var creds = SpotifyAuthentication.getAccessAndRefreshTokens("", spotifyProperties);
    if (StringUtils.hasLength(spotifyProperties.getRefreshToken())) {
      var credentials = dao.get().orElseGet(() -> dao.create(spotifyProperties.getRefreshToken()));
      var spotifyApi = SpotifyAuthentication.getRefreshedTokenAuth(spotifyProperties);
      if (spotifyApi.getRefreshToken() != null) {
        dao.updateRefreshToken(credentials.id(), spotifyApi.getRefreshToken());
      }
      return spotifyApi;
    }
    throw new IllegalStateException("Unable to setup Spotify auth.");
  }

  @Bean
  SpotifyApiContext spotifyRequestContext(SpotifyApi spotifyApi) {
    return new SpotifyApiContext(spotifyApi);
  }

  @Bean
  SpotifyTracksSearch spotifyTracksSearch(SpotifyApiContext spotifyApiContext, AudioFileDao audioFileDao) {
    return new SpotifyTracksSearch(spotifyApiContext, audioFileDao);
  }

  @Bean
  SpotifyLibrarySynchronizer spotifyPlaylistSynchronizer(SpotifyApiContext spotifyApiContext,
                                                         AudioFileDao audioFileDao,
                                                         PlayqdProperties playqdProperties,
                                                         SpotifyTracksSearch spotifyTracksSearch) {
    return new SpotifyLibrarySynchronizer(audioFileDao, playqdProperties, spotifyApiContext, spotifyTracksSearch);
  }
}
