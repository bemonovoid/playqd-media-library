package io.playqd.service.spotify.client;

import lombok.Getter;

public class SpotifyResponse<T> {

  @Getter
  private final T data;
  private final Throwable error;

  private SpotifyResponse(T data, Throwable error) {
    this.data = data;
    this.error = error;
  }

  public static <T> SpotifyResponse<T> success(T data) {
    return new SpotifyResponse<>(data, null);
  }

  public static <T> SpotifyResponse<T> error(Throwable t) {
    return new SpotifyResponse<T>(null, t);
  }

  public boolean hasError() {
    return error != null;
  }

  public boolean isSuccess() {
    return !hasError();
  }

  public Throwable getError() {
    return error;
  }

}
