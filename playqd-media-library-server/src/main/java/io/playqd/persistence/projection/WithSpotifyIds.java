package io.playqd.persistence.projection;

public sealed interface WithSpotifyIds extends AudioFileProjection permits AudioFileWithNamesAndSpotifyIds {

  String spotifyArtistId();

  String spotifyAlbumId();

  String spotifyTrackId();
}
