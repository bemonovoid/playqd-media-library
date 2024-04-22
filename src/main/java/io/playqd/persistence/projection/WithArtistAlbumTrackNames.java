package io.playqd.persistence.projection;

import io.playqd.persistence.projection.ArtistAlbumTrackNames;
import io.playqd.persistence.projection.AudioFileWithNamesAndSpotifyIds;

public sealed interface WithArtistAlbumTrackNames extends AudioFileProjection permits AudioFileWithNamesAndSpotifyIds {

  ArtistAlbumTrackNames names();
}
