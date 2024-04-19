package io.playqd.commons.data;

import lombok.Builder;

@Builder
public record ArtistQueryParams(String genreId) {

  public static ArtistQueryParams none() {
    return ArtistQueryParams.builder().build();
  }
}
