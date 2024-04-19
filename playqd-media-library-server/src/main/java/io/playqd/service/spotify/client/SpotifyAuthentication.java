package io.playqd.service.spotify.client;

import io.playqd.config.properties.SpotifyProperties;
import lombok.extern.slf4j.Slf4j;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.enums.AuthorizationScope;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.net.URI;

@Slf4j
public final class SpotifyAuthentication {

  public static SpotifyApi getApplicationAuth(SpotifyProperties spotifyProperties) {
    return new SpotifyApi.Builder()
        .setClientId(spotifyProperties.getClientId())
        .setClientSecret(spotifyProperties.getSecret())
        .setRedirectUri(SpotifyHttpManager.makeUri(spotifyProperties.getRedirectUri()))
        .build();
  }

  public static SpotifyApi getRefreshedTokenAuth(SpotifyProperties spotifyProperties) {
    var spotifyApi = new SpotifyApi.Builder()
        .setClientId(spotifyProperties.getClientId())
        .setClientSecret(spotifyProperties.getSecret())
        .setRefreshToken(spotifyProperties.getRefreshToken())
        .build();
    try {
      var authCodeCredentials = spotifyApi.authorizationCodeRefresh().build().execute();
      spotifyApi.setAccessToken(authCodeCredentials.getAccessToken());
      if (authCodeCredentials.getRefreshToken() != null) {
        log.info("New refresh token was issued.");
        spotifyApi.setRefreshToken(authCodeCredentials.getRefreshToken());
      }
      return spotifyApi;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static URI getAuthorizationCodeUri(SpotifyProperties spotifyProperties) {
    var spotifyApi = getApplicationAuth(spotifyProperties);
    return spotifyApi.authorizationCodeUri()
        .scope(
            AuthorizationScope.PLAYLIST_READ_PRIVATE,
            AuthorizationScope.PLAYLIST_MODIFY_PUBLIC,
            AuthorizationScope.PLAYLIST_MODIFY_PRIVATE,
            AuthorizationScope.USER_LIBRARY_READ,
            AuthorizationScope.USER_LIBRARY_MODIFY)
        .build().execute();
  }

  public static AuthorizationCodeCredentials getAccessAndRefreshTokens(String code,
                                                                       SpotifyProperties spotifyProperties) {
    var spotifyApi = getApplicationAuth(spotifyProperties);
    try {
      return spotifyApi.authorizationCode(code).build().execute();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

}
