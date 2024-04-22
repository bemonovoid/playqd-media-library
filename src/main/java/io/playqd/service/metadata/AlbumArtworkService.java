package io.playqd.service.metadata;

import io.playqd.service.metadata.Artwork;
import io.playqd.service.metadata.ArtworkKey;

import java.util.Optional;

public interface AlbumArtworkService {

  Optional<Artwork> getArtwork(ArtworkKey artworkKey);
}
