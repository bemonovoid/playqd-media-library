package io.playqd.controller;

import io.playqd.commons.data.Playlists;
import io.playqd.service.playlist.PlaylistService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/playlists")
class PlaylistController {

  private final PlaylistService playlistService;

  PlaylistController(PlaylistService playlistService) {
    this.playlistService = playlistService;
  }

  @GetMapping
  Playlists getAllPlaylists() {
    return playlistService.getPlaylists();
  }

  @PostMapping("/refresh")
  @ResponseStatus(HttpStatus.ACCEPTED)
  Playlists refresh() {
    return playlistService.refreshPlaylists();
  }
}
