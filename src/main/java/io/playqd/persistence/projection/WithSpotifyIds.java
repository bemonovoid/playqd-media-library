package io.playqd.persistence.projection;

import io.playqd.persistence.projection.AudioFileProjection;
import io.playqd.persistence.projection.AudioFileWithNamesAndSpotifyIds;

public sealed interface WithSpotifyIds extends AudioFileProjection permits AudioFileWithNamesAndSpotifyIds {

  String spotifyArtistId();

  String spotifyAlbumId();

  String spotifyTrackId();
}
