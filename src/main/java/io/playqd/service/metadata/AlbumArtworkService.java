package io.playqd.service.metadata;

import io.playqd.model.AudioFile;

import java.util.Optional;

public interface AlbumArtworkService {

  Optional<Artwork> get(String albumId);

  Optional<Artwork> get(AudioFile audioFile);

  Optional<Artwork> get(String albumId, String albumFolderImageFileName);
}
