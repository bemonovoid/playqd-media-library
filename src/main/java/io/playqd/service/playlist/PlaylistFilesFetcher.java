package io.playqd.service.playlist;

import io.playqd.commons.data.Playlist;

import java.util.List;

public interface PlaylistFilesFetcher {

  List<Playlist> fetch();
}
