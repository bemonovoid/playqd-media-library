package io.playqd.api.controller;

import io.playqd.api.controller.response.AddedMetadataResponse;
import io.playqd.commons.data.Album;
import io.playqd.commons.data.AlbumQueryParams;
import io.playqd.commons.data.Artist;
import io.playqd.commons.data.ArtistQueryParams;
import io.playqd.commons.data.Genre;
import io.playqd.commons.data.Track;
import io.playqd.model.AudioFile;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.MetadataReaderDao;
import io.playqd.persistence.WatchFolderFileEventLogDao;
import io.playqd.service.metadata.AlbumArtworkService;
import io.playqd.service.metadata.ImageSizeRequestParam;
import io.playqd.service.playlist.PlaylistService;
import io.playqd.util.FileUtils;
import io.playqd.util.TimeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/v1/metadata")
class MetadataController {

  private final AudioFileDao audioFileDao;
  private final PlaylistService playlistService;
  private final AlbumArtworkService artworkService;
  private final MetadataReaderDao metadataReaderDao;
  private final WatchFolderFileEventLogDao watchFolderFileEventLogDao;

  MetadataController(AudioFileDao audioFileDao,
                     PlaylistService playlistService,
                     AlbumArtworkService artworkService,
                     MetadataReaderDao metadataReaderDao,
                     WatchFolderFileEventLogDao watchFolderFileEventLogDao) {
    this.audioFileDao = audioFileDao;
    this.playlistService = playlistService;
    this.artworkService = artworkService;
    this.metadataReaderDao = metadataReaderDao;
    this.watchFolderFileEventLogDao = watchFolderFileEventLogDao;
  }

//  @GetMapping("/sources/{sourceId}/info")
//  MetadataContentInfo info(@PathVariable long sourceId) {
//    return mediaMetadataService.getInfo(sourceId);
//  }
//
//  @DeleteMapping("/sources/{sourceId}")
//  @ResponseStatus(HttpStatus.NO_CONTENT)
//  String clear(@PathVariable long sourceId) {
//    return String.format("Successfully removed %s metadata items from store.", mediaMetadataService.clear(sourceId));
//  }

  @GetMapping("/artists")
  Page<Artist> artists(@PageableDefault(size = 100, sort = "name") Pageable page, ArtistQueryParams params) {
    if (StringUtils.hasLength(params.genreId())) {
      return audioFileDao.getGenreArtists(params.genreId(), page);
    }
    return metadataReaderDao.getArtists(page);
  }

  @GetMapping("/albums")
  Page<Album> albums(@PageableDefault(size = 100, sort = "name") Pageable page, AlbumQueryParams params) {
    if (StringUtils.hasLength(params.artistId())) {
      return metadataReaderDao.getAlbumsByArtistId(params.artistId(), page);
    }
    if (StringUtils.hasLength(params.genreId())) {
      return metadataReaderDao.getAlbumsByGenreId(params.genreId(), page);
    }
    return metadataReaderDao.getAlbums(page);
  }

  @GetMapping("/genres")
  Page<Genre> getGenres(@PageableDefault(size = 100, sort = "name") Pageable page) {
    return metadataReaderDao.getGenres(page);
  }

  @GetMapping("/tracks")
  Page<Track> tracks(@PageableDefault(size = 100, sort = "name") Pageable page,
                     @RequestParam(name = "artistId", required = false) String artistId,
                     @RequestParam(name = "albumId", required = false) String albumId,
                     @RequestParam(name = "genreId", required = false) String genreId,
                     @RequestParam(name = "playlistId", required = false) String playlistId,
                     @RequestParam(name = "title", required = false) String title,
                     @RequestParam(name = "played", required = false) Boolean played,
                     @RequestParam(name = "lastRecentlyAdded", required = false) boolean lastRecentlyAdded,
                     @RequestParam(name = "recentlyAddedSinceDuration", required = false) String recentlyAddedSinceDuration,
                     @RequestParam(name = "locations", required = false) Set<String> locations) {
    if (StringUtils.hasLength(artistId)) {
      return audioFileDao.getAudioFilesByArtistId(artistId, page).map(this::mapToTrack);
    }

    if (StringUtils.hasLength(albumId)) {
      return audioFileDao.getAudioFilesByAlbumId(albumId, page).map(this::mapToTrack);
    }

    if (StringUtils.hasLength(genreId)) {
      return audioFileDao.getAudioFilesByGenreId(genreId, page).map(this::mapToTrack);
    }

    if (StringUtils.hasLength(playlistId)) {
      return playlistService.getPlaylistAudioFiles(playlistId, page).map(this::mapToTrack);
    }

    if (StringUtils.hasText(title)) {
      return audioFileDao.getAudioFilesByTitle(title, page).map(this::mapToTrack);
    }

    if (played != null) {
      return audioFileDao.getAudioFilesByPlayed(played, page).map(this::mapToTrack);
    }

    if (!CollectionUtils.isEmpty(locations)) {
      return audioFileDao.getAudioFilesByLocationIn(locations, page).map(this::mapToTrack);
    }

    if (lastRecentlyAdded) {
      if (watchFolderFileEventLogDao.hasEvents()) {
        var dateAfter = watchFolderFileEventLogDao.getLastAddedDate().minusDays(1);
        return audioFileDao.getAudioFilesAddedToWatchFolderAfterDate(dateAfter, page).map(this::mapToTrack);
      }
      return Page.empty();
    }

    if (StringUtils.hasText(recentlyAddedSinceDuration)) {
      var duration = Duration.parse(recentlyAddedSinceDuration);
      var dateAfter = LocalDate.now().minusDays(duration.toDays());
      return audioFileDao.getAudioFilesAddedToWatchFolderAfterDate(dateAfter, page).map(this::mapToTrack);
    }

    return audioFileDao.getAudioFiles(page).map(this::mapToTrack);
  }

  @GetMapping("/tracks/added")
  AddedMetadataResponse recentlyAdded(@RequestParam(required = false, name = "durationFromNow", defaultValue = "PT3M")
                                      Duration durationFromNow) {
    var dateAfter = LocalDate.now().minusDays(durationFromNow.toDays() + 1);

    var albumsAddedAfter = audioFileDao.getAlbumsAddedAfterDate(dateAfter);

    var albumsGroupedByAddedDate = albumsAddedAfter.stream().collect(Collectors.groupingBy(
        Album::addedToWatchFolderDate,
        Collectors.mapping(album -> new AddedMetadataResponse.MetadataItem(
                album.artistId(),
                album.artistName(),
                album.id(),
                album.name()),
            Collectors.toList())
    ));

    var items = albumsGroupedByAddedDate.entrySet().stream()
        .map(entry -> new AddedMetadataResponse.AddedItem(entry.getKey(), entry.getValue()))
        .sorted()
        .toList();

    return new AddedMetadataResponse(
        new AddedMetadataResponse.SearchPeriod(dateAfter, LocalDate.now()),
        albumsAddedAfter.stream().mapToInt(Album::tracksCount).sum(),
        items);
  }

  private Track mapToTrack(AudioFile audioFile) {

    var artist = new Track.Artist(audioFile.artistId(), audioFile.artistName());

    var artwork = artworkService.get(audioFile.albumId())
        .map(art -> new Track.Artwork(
            art.albumId(),
            art.metadata().mimeType(),
            art.metadata().size(),
            art.resources().getResizedOrOriginal(ImageSizeRequestParam.sm).uri()))
        .orElse(null);

    var album = new Track.Album(
        audioFile.albumId(),
        audioFile.albumName(),
        audioFile.genreId(),
        audioFile.genre(),
        artwork);

    var length = new Track.Length(audioFile.trackLength(),
        audioFile.preciseTrackLength(),
        TimeUtils.durationToTimeFormat(Duration.ofSeconds(audioFile.trackLength())));

    var playback = new Track.Playback(audioFile.playbackCount(), toTrackLastPlayedDate(audioFile));

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
        audioFormat,
        fileAttributes,
        additionalInfo);
  }

  private static LocalDateTime toTrackLastPlayedDate(AudioFile audioFile) {
    if (audioFile.fileLastPlaybackDate() == null) {
      return null;
    }
    return LocalDateTime.ofInstant(audioFile.fileLastPlaybackDate(), ZoneId.systemDefault());
  }

}
