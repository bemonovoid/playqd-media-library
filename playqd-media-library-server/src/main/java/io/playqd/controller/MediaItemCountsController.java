package io.playqd.controller;

import io.playqd.commons.data.MediaItemsCount;
import io.playqd.persistence.MediaLibraryDao;
import io.playqd.service.playlist.PlaylistService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/counts")
class MediaItemCountsController {

  private final MediaLibraryDao mediaLibraryDao;
  private final PlaylistService playlistService;

  MediaItemCountsController(MediaLibraryDao mediaLibraryDao,
                            PlaylistService playlistService) {
    this.mediaLibraryDao = mediaLibraryDao;
    this.playlistService = playlistService;
  }

  @GetMapping
  LibraryCounts all() {
    return new LibraryCounts(
        new MediaItemsCount(mediaLibraryDao.ofArtist().count()),
        new MediaItemsCount(mediaLibraryDao.ofAlbum().count()),
        new MediaItemsCount(mediaLibraryDao.ofGenre().count()),
        new MediaItemsCount(mediaLibraryDao.ofAudioFile().count()),
        new MediaItemsCount(playlistService.getPlaylists().playlists().size()));
  }

  record LibraryCounts(MediaItemsCount artists,
                       MediaItemsCount albums,
                       MediaItemsCount genres,
                       MediaItemsCount tracks,
                       MediaItemsCount playlists) {
  };
}