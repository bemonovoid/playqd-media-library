package io.playqd.service.playlist;

import io.playqd.commons.data.Playlist;

import java.util.List;

public interface PlaylistFetcher {

  List<Playlist> fetch();
}
