package io.playqd.persistence;

import io.playqd.commons.data.Playlist;

import java.util.List;
import java.util.Optional;

public interface PlaylistDao {

  Optional<Playlist> getById(long id);

  Optional<Playlist> getByUUID(String uuid);

  List<Playlist> getPlaylists();

  Playlist create(Playlist playlist);

  Playlist update(Playlist playlist);

  void delete(Playlist playlist);
}
