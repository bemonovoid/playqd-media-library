package io.playqd.commons.data;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum PlaylistFormat {

  m3u,

  m3u8,

  spotify,

  unknown;

  public static PlaylistFormat fromString(String format) {
    try {
      return PlaylistFormat.valueOf(format);
    } catch (IllegalArgumentException e) {
      log.error("Unsupported playlist format: {}", format);
      return PlaylistFormat.unknown;
    }
  }

}
