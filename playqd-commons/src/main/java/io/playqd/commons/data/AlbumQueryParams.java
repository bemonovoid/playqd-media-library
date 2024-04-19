package io.playqd.commons.data;

import lombok.Builder;

@Builder
public record AlbumQueryParams(String artistId, String genreId) {

  public static AlbumQueryParams none() {
    return AlbumQueryParams.builder().build();
  }
}
