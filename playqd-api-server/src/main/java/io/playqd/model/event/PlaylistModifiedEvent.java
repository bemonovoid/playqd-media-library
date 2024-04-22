package io.playqd.model.event;

import io.playqd.commons.data.Playlist;

public record PlaylistModifiedEvent(Playlist oldPlaylist, Playlist newPlaylist) {
}
