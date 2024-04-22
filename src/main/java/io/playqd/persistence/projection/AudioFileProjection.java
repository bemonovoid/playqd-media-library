package io.playqd.persistence.projection;

import io.playqd.persistence.projection.WithArtistAlbumTrackNames;
import jakarta.persistence.Transient;

import java.nio.file.Path;
import java.nio.file.Paths;

public sealed interface AudioFileProjection permits
    WithMimeType,
    WithLastModifiedDate,
    WithArtistAlbumTrackNames,
    WithSpotifyIds {

  long id();

  String location();

  @Transient
  default Path path() {
    return Paths.get(location());
  }
}
