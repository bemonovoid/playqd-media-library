package io.playqd.api.controller;

import io.playqd.commons.data.MediaItemsCount;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.MetadataReaderDao;
import io.playqd.persistence.WatchFolderFileEventLogDao;
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

  MetadataCountsController(AudioFileDao audioFileDao,
                           MetadataReaderDao metadataReaderDao,
                           WatchFolderFileEventLogDao watchFolderFileEventLogDao) {
    this.audioFileDao = audioFileDao;
    this.metadataReaderDao = metadataReaderDao;
    this.watchFolderFileEventLogDao = watchFolderFileEventLogDao;
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
}
