package io.playqd.api.controller;

import io.playqd.api.controller.response.AddedMetadataResponse;
import io.playqd.api.controller.response.MediaItemsCount;
import io.playqd.commons.data.Album;
import io.playqd.commons.data.Artist;
import io.playqd.commons.data.Genre;
import io.playqd.commons.data.Track;
import io.playqd.model.AudioFile;
import io.playqd.model.MediaItemFilter;
import io.playqd.model.MediaItemType;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.Filter;
import io.playqd.persistence.MetadataReaderDao;
import io.playqd.persistence.jpa.entity.view.ArtistViewEntity;
import io.playqd.service.metadata.AlbumArtworkService;
import io.playqd.service.metadata.ImageSizeRequestParam;
import io.playqd.service.metadata.MediaMetadataService;
import io.playqd.service.metadata.MetadataContentInfo;
import io.playqd.util.FileUtils;
import io.playqd.util.TimeUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
  private final AlbumArtworkService artworkService;
  private final MetadataReaderDao metadataReaderDao;
  private final MediaMetadataService mediaMetadataService;

  MetadataController(AudioFileDao audioFileDao,
                     AlbumArtworkService artworkService,
                     MetadataReaderDao metadataReaderDao,
                     MediaMetadataService mediaMetadataService) {
    this.audioFileDao = audioFileDao;
    this.artworkService = artworkService;
    this.metadataReaderDao = metadataReaderDao;
    this.mediaMetadataService = mediaMetadataService;
  }

  @GetMapping("/sources/{sourceId}/info")
  MetadataContentInfo info(@PathVariable long sourceId) {
    return mediaMetadataService.getInfo(sourceId);
  }

  @DeleteMapping("/sources/{sourceId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  String clear(@PathVariable long sourceId) {
    return String.format("Successfully removed %s metadata items from store.", mediaMetadataService.clear(sourceId));
  }

  @GetMapping("/artists")
  Page<Artist> artists(@PageableDefault(size = 100, sort = "name") Pageable page) {
    return metadataReaderDao.getArtists(page);
  }

  @GetMapping("/counts/{mediaItemType}")
  MediaItemsCount artistsCount(@PathVariable MediaItemType mediaItemType,
                              @RequestParam(name = "filter", required = false) MediaItemFilter filter) {
    return new MediaItemsCount(metadataReaderDao.count(mediaItemType, filter));
  }

  @GetMapping("/albums")
  Page<Album> albums(@PageableDefault(size = 100, sort = "name") Pageable page,
                         @RequestParam(name = "artistId", required = false, defaultValue = "") String artistId,
                         @RequestParam(name = "genreId", required = false, defaultValue = "") String genreId) {
    if (!artistId.isEmpty()) {
      return metadataReaderDao.getAlbumsByArtistId(artistId, page);
    } else if (!genreId.isEmpty()) {
      return metadataReaderDao.getAlbumsByGenreId(genreId, page);
    }
    return metadataReaderDao.getAlbums(page);
  }

  @GetMapping("/genres")
  Page<Genre> getGenres(@PageableDefault(size = 100, sort = "name") Pageable page) {
    return metadataReaderDao.getGenres(page);
  }

  @GetMapping("/tracks")
  Page<Track> tracks(@PageableDefault(size = 100, sort = "name") Pageable page,
                        @RequestParam(name = "filter", required = false, defaultValue = "") String filter,
                        @RequestParam(name = "artistId", required = false, defaultValue = "") String artistId,
                        @RequestParam(name = "albumId", required = false, defaultValue = "") String albumId,
                        @RequestParam(name = "locations", required = false) Set<String> locations) {
    if (StringUtils.hasLength(artistId)) {
      return new PageImpl<>(audioFileDao.getAudioFilesByArtistId(artistId)).map(this::mapToTrack);
    } else if (StringUtils.hasLength(albumId)) {
      return new PageImpl<>(audioFileDao.getAudioFilesByAlbumId(albumId)).map(this::mapToTrack);
    } else if (!CollectionUtils.isEmpty(locations)) {
      return audioFileDao.getAudioFilesByLocationIn(locations).map(this::mapToTrack);
    }
    return audioFileDao.getAudioFiles(new Filter(filter), page).map(this::mapToTrack);
  }

  @GetMapping("/recentlyAdded")
  AddedMetadataResponse recentlyAdded(@RequestParam(required = false, name = "lastNumberOfMonths", defaultValue = "3")
                                      @Min(1) @Max(12)
                                      int lastNumberOfMonths) {
    var dateAfter = LocalDate.now().minusMonths(lastNumberOfMonths).minusDays(1);

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

    var playback = new Track.Playback(
        audioFile.playbackCount(),
        audioFile.fileLastPlaybackDate() == null ? null : LocalDateTime.ofInstant(audioFile.fileLastPlaybackDate(), ZoneId.systemDefault()));

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

}
