package io.playqd.api.controller;

import io.playqd.commons.data.Playlist;
import io.playqd.service.playlist.PlaylistService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/playlists")
class PlaylistController {

  private final PlaylistService playlistService;

  PlaylistController(PlaylistService playlistService) {
    this.playlistService = playlistService;
  }

  @GetMapping
  List<Playlist> getAllPlaylists() {
    return playlistService.getPlaylists();
  }
}
