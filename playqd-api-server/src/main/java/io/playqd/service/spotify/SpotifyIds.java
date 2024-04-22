package io.playqd.service.spotify;

import lombok.Builder;

@Builder
public record SpotifyIds(String artistId, String albumId, String trackId) {
}
