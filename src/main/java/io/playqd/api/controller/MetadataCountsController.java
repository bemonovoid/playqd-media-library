package io.playqd.api.controller;

import io.playqd.commons.data.MediaItemsCount;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.MetadataReaderDao;
import io.playqd.persistence.WatchFolderFileEventLogDao;
import io.playqd.service.playlist.PlaylistService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/metadata/counts")
class MetadataCountsController {

  private final AudioFileDao audioFileDao;
  private final MetadataReaderDao metadataReaderDao;
  private final WatchFolderFileEventLogDao watchFolderFileEventLogDao;
  private final PlaylistService playlistService;

  MetadataCountsController(AudioFileDao audioFileDao,
                           PlaylistService playlistService,
                           MetadataReaderDao metadataReaderDao,
                           WatchFolderFileEventLogDao watchFolderFileEventLogDao) {
    this.audioFileDao = audioFileDao;
    this.playlistService = playlistService;
    this.metadataReaderDao = metadataReaderDao;
    this.watchFolderFileEventLogDao = watchFolderFileEventLogDao;
  }

  @GetMapping
  LibraryCounts all() {
    return new LibraryCounts(artistsCount(), albumsCount(), genresCount(), tracks(), playlists());
  }

  @GetMapping("/artists")
  MediaItemsCount artistsCount() {
    return new MediaItemsCount(metadataReaderDao.countArtists());
  }

  @GetMapping("/albums")
  MediaItemsCount albumsCount() {
    return new MediaItemsCount(metadataReaderDao.countAlbums());
  }

  @GetMapping("/genres")
  MediaItemsCount genresCount() {
    return new MediaItemsCount(metadataReaderDao.countGenres());
  }

  @GetMapping("/tracks")
  MediaItemsCount tracks() {
    return new MediaItemsCount(audioFileDao.count());
  }

  @GetMapping("/tracks/played")
  MediaItemsCount tracksPlayedCount() {
    return new MediaItemsCount(audioFileDao.countPlayed());
  }

  @GetMapping("/tracks/liked")
  MediaItemsCount tracksLikedCount() {
    return new MediaItemsCount(0);
  }

  @GetMapping("/tracks/lastRecentlyAdded")
  MediaItemsCount tracksLastRecentlyAddedCount() {
    var afterDate = watchFolderFileEventLogDao.getLastAddedDate();
    return new MediaItemsCount(audioFileDao.countByAddedToWatchFolderSinceDate(afterDate));
  }

  @GetMapping("/playlists")
  MediaItemsCount playlists() {
    return new MediaItemsCount(playlistService.count());
  }

  record LibraryCounts(MediaItemsCount artists,
                       MediaItemsCount albums,
                       MediaItemsCount genres,
                       MediaItemsCount tracks,
                       MediaItemsCount playlists) {

  };
}
