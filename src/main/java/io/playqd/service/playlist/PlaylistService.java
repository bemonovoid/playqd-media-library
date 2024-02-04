package io.playqd.service.playlist;

import io.playqd.model.AudioFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlaylistService {

  Page<AudioFile> getPlaylistAudioFiles(String playlistId, Pageable pageable);
}
