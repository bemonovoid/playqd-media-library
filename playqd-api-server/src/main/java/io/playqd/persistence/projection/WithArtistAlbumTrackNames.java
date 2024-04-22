package io.playqd.persistence.projection;

public sealed interface WithArtistAlbumTrackNames extends AudioFileProjection permits AudioFileWithNamesAndSpotifyIds {

  ArtistAlbumTrackNames names();
}
