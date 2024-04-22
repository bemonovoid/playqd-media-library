package io.playqd.service.playlist;

import io.playqd.commons.data.Playlist;
import io.playqd.commons.data.Playlists;

import java.util.Optional;

public interface PlaylistService {

  Playlists getPlaylists();

  Playlists refreshPlaylists();

  Optional<Playlist> getById(long id);

  Optional<Playlist> getByUUID(String uuid);

}
