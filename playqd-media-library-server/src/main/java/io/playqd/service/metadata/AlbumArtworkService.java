package io.playqd.service.metadata;

import java.util.Optional;

public interface AlbumArtworkService {

  Optional<Artwork> getArtwork(ArtworkKey artworkKey);
}
