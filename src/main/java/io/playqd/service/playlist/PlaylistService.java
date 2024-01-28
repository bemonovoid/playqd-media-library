package io.playqd.service.playlist;

import io.playqd.model.AudioFile;

import java.util.List;

public interface PlaylistService {

  List<AudioFile> getPlaylistAudioFiles(String playlistId);
}
