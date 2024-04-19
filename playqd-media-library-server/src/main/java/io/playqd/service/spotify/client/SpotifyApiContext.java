package io.playqd.service.spotify.client;

import io.playqd.exception.PlayqdException;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.UnauthorizedException;
import se.michaelthelin.spotify.requests.data.AbstractDataRequest;

import java.io.IOException;
import java.util.function.Function;

@Slf4j
public class SpotifyApiContext {

  private final SpotifyApi spotifyApi;

  public SpotifyApiContext(SpotifyApi spotifyApi) {
    this.spotifyApi = spotifyApi;
  }

  public synchronized <T> SpotifyResponse<T> executeOrThrow(Function<SpotifyApi, AbstractDataRequest<T>> func) {
    var response = execute(func);
    if (response.hasError()) {
      throw new PlayqdException(response.getError());
    }
    return response;
  }

  public synchronized <T> SpotifyResponse<T> execute(Function<SpotifyApi, AbstractDataRequest<T>> func) {
    var request = func.apply(spotifyApi);
    try {
      return SpotifyResponse.success(request.execute());
    } catch (UnauthorizedException e) {
      if (e.getMessage().contains("token")) {
        System.out.println("ASdasd");
      }
      return SpotifyResponse.error(e);
    }
    catch (IOException | ParseException | SpotifyWebApiException e) {
      log.error("Spotify '{}' request failed. {}", request.getClass().getName(), e.getMessage(), e);
      return SpotifyResponse.error(e);
    }
  }
}
