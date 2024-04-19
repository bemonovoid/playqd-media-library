package io.playqd.controller;

import io.playqd.commons.data.ItemType;
import io.playqd.commons.data.Track;
import io.playqd.model.AudioFile;
import io.playqd.model.event.AudioFileByteStreamRequestedEvent;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.WatchFolderFileEventLogDao;
import io.playqd.persistence.projection.AudioFileWithMimeType;
import io.playqd.service.playlist.PlaylistService;
import io.playqd.service.watchfolder.WatchFolderBrowser;
import io.playqd.util.FileUtils;
import io.playqd.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodProcessor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/tracks")
class TrackController {

  private final AudioFileDao audioFileDao;
  private final PlaylistService playlistService;
  private final WatchFolderBrowser watchFolderBrowser;
  private final ApplicationEventPublisher eventPublisher;
  private final WatchFolderFileEventLogDao watchFolderFileEventLogDao;

  TrackController(AudioFileDao audioFileDao,
                  PlaylistService playlistService,
                  WatchFolderBrowser watchFolderBrowser,
                  ApplicationEventPublisher eventPublisher,
                  WatchFolderFileEventLogDao watchFolderFileEventLogDao) {
    this.audioFileDao = audioFileDao;
    this.eventPublisher = eventPublisher;
    this.playlistService = playlistService;
    this.watchFolderBrowser = watchFolderBrowser;
    this.watchFolderFileEventLogDao = watchFolderFileEventLogDao;
  }

  @GetMapping("/artists/{artistId}")
  Page<Track> artistTracks(@PathVariable(name = "artistId") String artistId,
                     @PageableDefault(size = 100, sort = "name") Pageable page) {
    return audioFileDao.getAudioFilesByArtistId(artistId, page).map(this::mapToTrack);
  }

  @GetMapping("/albums/{albumId}")
  Page<Track> albumTracks(@PathVariable(name = "albumId") String albumId,
                     @PageableDefault(size = 100, sort = "name") Pageable page) {
    return audioFileDao.getAudioFilesByAlbumId(albumId, page).map(this::mapToTrack);
  }

  @GetMapping("/genres/{genreId}")
  Page<Track> genreTracks(@PathVariable(name = "genreId") String genreId,
                          @PageableDefault(size = 100, sort = "name") Pageable page) {
    return audioFileDao.getAudioFilesByGenreId(genreId, page).map(this::mapToTrack);
  }

  @GetMapping("/playlists/{playlistId}")
  Page<Track> playlistTracks(@PathVariable(name = "playlistId") String playlistId,
                             @PageableDefault(size = 100, sort = "name") Pageable page) {
    return playlistService.getByUUID(playlistId)
        .map(p -> audioFileDao.getAudioFilesFromPlaylist(p, page).map(this::mapToTrack))
        .orElse(Page.empty());
  }

  @GetMapping("/folders/{folderId}")
  Page<Track> playedTracks(@PathVariable(name = "folderId") String folderId,
                           @PageableDefault(size = 100, sort = "name") Pageable page) {
    var audioItems = watchFolderBrowser.browse(folderId, ItemType.audioFile);
    var locations = audioItems.stream()
        .map(watchFolderItem -> watchFolderItem.path().toString())
        .toList();
    return audioFileDao.getAudioFilesByLocationIn(locations, page).map(this::mapToTrack);
  }

  @GetMapping("/played")
  Page<Track> playedTracks(@PageableDefault(size = 100, sort = "name") Pageable page) {
    return audioFileDao.getPlayedAudioFiles(page).map(this::mapToTrack);
  }

  @GetMapping("/rated")
  Page<Track> ratedTracks(@PageableDefault(size = 100, sort = "name") Pageable page) {
    return audioFileDao.getAudioFilesWithRating(page).map(this::mapToTrack);
  }

  @GetMapping("/saved")
  Page<Track> savedTracks(@PageableDefault(size = 100, sort = "name") Pageable page) {
    return Page.empty();
  }

  @GetMapping("/recentlyAdded")
  Page<Track> recentlyAddedTracks(@PageableDefault(size = 100, sort = "name") Pageable page,
                                  @RequestParam(required = false, name = "durationFromNow") //"PT3M"
                                  Duration duration) {
    if (duration != null) {
      var dateAfter = LocalDate.now().minusDays(duration.toDays());
      return audioFileDao.getAudioFilesAddedToWatchFolderAfterDate(dateAfter, page).map(this::mapToTrack);
    }
    if (watchFolderFileEventLogDao.hasEvents()) {
      var dateAfter = watchFolderFileEventLogDao.getLastAddedDate().minusDays(1);
      return audioFileDao.getAudioFilesAddedToWatchFolderAfterDate(dateAfter, page).map(this::mapToTrack);
    }
    return Page.empty();
  }

  @GetMapping("/tracks")
  Page<Track> tracks(@PageableDefault(size = 100, sort = "name") Pageable page,
                     @RequestParam(name = "title", required = false) String title) {
    if (StringUtils.hasText(title)) {
      return audioFileDao.getAudioFilesByTitle(title, page).map(this::mapToTrack);
    }
    return audioFileDao.getAudioFiles(page).map(this::mapToTrack);
  }

  /**
   * See: Spring's {@link AbstractMessageConverterMethodProcessor} (line: 186 & 194) implementation that handles byte ranges
   *
   * @param trackId
   * @param httpHeaders
   * @return Audio file stream at the given byte range.
   */
  @GetMapping("/{trackId}/file")
  ResponseEntity<Resource> audioTrackStream(@PathVariable String trackId, @RequestHeader HttpHeaders httpHeaders) {

    var audioFile = audioFileDao.getAudioFileDataByTrackId(trackId, AudioFileWithMimeType.class);
    var audioFilePath = audioFile.path();

    log.info("\n---Processed audio streaming info---\nTrack id: {}\nRange: {}\nResource externalUrl: {}\nContent-Type: {}",
        trackId,
        Arrays.toString(httpHeaders.getRange().toArray()),
        audioFilePath,
        audioFile.mimeType());

    getHttpRangeRequestIfExists(httpHeaders).ifPresentOrElse(
        httpRange -> {
          // 'getRangeStart' length is ignored for ByteRange and can be anything.
          if (httpRange.getRangeStart(0) == 0) {
            eventPublisher.publishEvent(new AudioFileByteStreamRequestedEvent(audioFile.id()));
          }
        },
        () -> eventPublisher.publishEvent(new AudioFileByteStreamRequestedEvent(audioFile.id())));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, audioFile.mimeType())
        .body(new FileSystemResource(audioFilePath));
  }

  private static Optional<HttpRange> getHttpRangeRequestIfExists(HttpHeaders httpHeaders) {
    if (httpHeaders.isEmpty()) {
      return Optional.empty();
    }
    if (CollectionUtils.isEmpty(httpHeaders.getRange())) {
      return Optional.empty();
    }
    if (httpHeaders.getRange().size() > 1) {
      log.warn("'Range' header contains multiple ranges. The first range is being used.");
    }
    if (!httpHeaders.getRange().getClass().getSimpleName().equals("ByteRange")) {
      return Optional.empty();
    }
    return Optional.of(httpHeaders.getRange().get(0));
  }

  private static LocalDateTime toTrackLastPlayedDate(AudioFile audioFile) {
    if (audioFile.fileLastPlaybackDate() == null) {
      return null;
    }
    return LocalDateTime.ofInstant(audioFile.fileLastPlaybackDate(), ZoneId.systemDefault());
  }

//  @GetMapping("/tracks/added")
//  AddedMetadataResponse recentlyAdded(@RequestParam(required = false, name = "durationFromNow", defaultValue = "PT3M")
//                                      Duration durationFromNow) {
//    var dateAfter = LocalDate.now().minusDays(durationFromNow.toDays() + 1);
//
//    var albumsAddedAfter = audioFileDao.getAlbumsAddedAfterDate(dateAfter);
//
//    var albumsGroupedByAddedDate = albumsAddedAfter.stream().collect(Collectors.groupingBy(
//        Album::addedToWatchFolderDate,
//        Collectors.mapping(album -> new AddedMetadataResponse.MetadataItem(
//                album.artistId(),
//                album.artistName(),
//                album.id(),
//                album.name()),
//            Collectors.toList())
//    ));
//
//    var items = albumsGroupedByAddedDate.entrySet().stream()
//        .map(entry -> new AddedMetadataResponse.AddedItem(entry.getKey(), entry.getValue()))
//        .sorted()
//        .toList();
//
//    return new AddedMetadataResponse(
//        new AddedMetadataResponse.SearchPeriod(dateAfter, LocalDate.now()),
//        albumsAddedAfter.stream().mapToInt(Album::tracksCount).sum(),
//        items);
//  }

  private Track mapToTrack(AudioFile audioFile) {

    var artist = new Track.Artist(audioFile.artistId(), audioFile.artistName());

    var album = new Track.Album(
        audioFile.albumId(),
        audioFile.albumName(),
        audioFile.genreId(),
        audioFile.genre());

    var length = new Track.Length(audioFile.trackLength(),
        audioFile.preciseTrackLength(),
        TimeUtils.durationToTimeFormat(Duration.ofSeconds(audioFile.trackLength())));

    var playback = new Track.Playback(audioFile.playbackCount(), toTrackLastPlayedDate(audioFile));
    var rating = new Track.Rating(audioFile.rating());

    var audioFormat = new Track.AudioFormat(
        audioFile.mimeType(),
        audioFile.bitRate(),
        audioFile.sampleRate(),
        audioFile.bitsPerSample());

    var fileAttributes = new Track.FileAttributes(
        audioFile.name(),
        audioFile.location(),
        audioFile.extension(),
        FileUtils.byteCountToDisplaySize(audioFile.size()));

    var additionalInfo = new Track.AdditionalInfo(audioFile.fileAddedToWatchFolderDate());

    return new Track(
        audioFile.id(),
        audioFile.trackId(),
        audioFile.trackName(),
        audioFile.trackNumber(),
        artist,
        album,
        length,
        playback,
        rating,
        audioFormat,
        fileAttributes,
        additionalInfo);
  }
}