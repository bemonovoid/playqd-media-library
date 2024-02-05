package io.playqd.service.playlist;

import io.playqd.commons.data.Playlist;

import java.util.List;

public interface PlaylistService {

  List<Playlist> getPlaylists();

  List<String> playlistFiles(String playlistId);
}
