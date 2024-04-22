package io.playqd.persistence.projection;

public record AudioFileWithNamesAndSpotifyIds(long id,
                                              String location,
                                              ArtistAlbumTrackNames names,
                                              String spotifyArtistId,
                                              String spotifyAlbumId,
                                              String spotifyTrackId)
    implements WithSpotifyIds, WithArtistAlbumTrackNames {
}
