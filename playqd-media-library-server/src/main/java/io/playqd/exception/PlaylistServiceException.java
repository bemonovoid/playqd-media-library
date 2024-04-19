package io.playqd.exception;

public class PlaylistServiceException extends PlayqdException {

  public PlaylistServiceException(String message) {
    super(message);
  }

  public PlaylistServiceException(Throwable cause) {
    super(cause);
  }

  public PlaylistServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
