package io.playqd.service.spotify;

import io.playqd.commons.utils.Tuple;
import io.playqd.model.AudioFile;
import io.playqd.persistence.AudioFileDao;
import io.playqd.service.spotify.client.SpotifyApiContext;
import io.playqd.service.spotify.client.SpotifyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public final class SpotifyTracksSearch {

  private final AudioFileDao audioFileDao;
  private final SpotifyApiContext spotifyApiContext;

  public SpotifyTracksSearch(SpotifyApiContext spotifyApiContext, AudioFileDao audioFileDao) {
    this.audioFileDao = audioFileDao;
    this.spotifyApiContext = spotifyApiContext;
  }

  public SpotifyTrackId search(AudioFile audioFile) {
    if (StringUtils.hasLength(audioFile.spotifyTrackId())) {
      return new SpotifyTrackId(audioFile.spotifyTrackId(), "spotify:track:" + audioFile.spotifyTrackId());
    }

    if (StringUtils.hasLength(audioFile.spotifyAlbumId())) {
      return searchInsideAlbum(audioFile);
    }

    if (StringUtils.hasLength(audioFile.spotifyArtistId())) {
      return searchInsideArtist(audioFile);
    }

    log.info("Searching '{}' track by artist name: '{}'.", audioFile.trackName(), audioFile.artistName());
    return searchByArtistAndTrackNames(audioFile);
  }

  private SpotifyTrackId searchInsideAlbum(AudioFile audioFile) {
    log.info("Searching '{}' track in artist: '{}' and album: '{}' by spotify album id: '{}'",
        audioFile.trackName(), audioFile.artistName(), audioFile.albumName(), audioFile.spotifyAlbumId());

    var albumId = audioFile.spotifyAlbumId();

    var spotifyResponse = spotifyApiContext.execute(spotifyApi -> spotifyApi.getAlbumsTracks(albumId).build());

    if (spotifyResponse.hasError()) {
      log.error("Failed to retrieve album tracks by album id.");
      return null;
    }

    var trackName = sanitizeTrackName(audioFile.trackName());

    var matchingTrack = Stream.of(spotifyResponse.getData().getItems())
        .filter(track -> track.getName().equalsIgnoreCase(trackName))
        .findFirst();

    matchingTrack.ifPresentOrElse(spotifyTrack -> {
      log.info("Found matching track with id: {}", spotifyTrack.getId());
      audioFileDao.updateSpotifyIds(audioFile, SpotifyIds.builder().trackId(spotifyTrack.getId()).build());
    }, () -> log.info("Track matching name: '{}' and album id: '{}' was not found.", trackName, albumId));

    return matchingTrack.map(t -> new SpotifyTrackId(t.getId(), t.getUri())).orElse(null);
  }

  private SpotifyTrackId searchInsideArtist(AudioFile audioFile) {
    var artistId = audioFile.spotifyArtistId();

    log.info("Searching '{}' track in artist: '{}' by spotify artist id: '{}'",
        audioFile.trackName(), audioFile.artistName(), artistId);

    var spotifyResponse = spotifyApiContext.execute(spotifyApi -> spotifyApi.getArtistsAlbums(artistId).build());

    if (spotifyResponse.hasError()) {
      log.error("Unable to get artist albums by artist id.");
      return null;
    } else {
      log.info("Found {} artist albums. {}", spotifyResponse.getData().getTotal(),
          Arrays.stream(spotifyResponse.getData().getItems())
              .map(AlbumSimplified::getName)
              .collect(Collectors.joining(", ")));
    }

    var trackName = sanitizeTrackName(audioFile.trackName());

    var matchingTrack = Stream.of(spotifyResponse.getData().getItems())
        .filter(album -> album.getName().equalsIgnoreCase(audioFile.albumName()))
        .map(album -> Tuple.from(
            album.getId(),
            spotifyApiContext.execute(spotifyApi -> spotifyApi.getAlbumsTracks(album.getId()).build())))
        .filter(t -> t.right().isSuccess())
        .peek(t -> log.info("Found album matching name: '{}'", audioFile.albumName()))
        .map(t -> Stream.of(t.right().getData().getItems())
            .filter(track -> track.getName().equalsIgnoreCase(trackName))
            .findFirst()
            .map(track -> Tuple.from(t.left(), track))
            .orElse(null))
        .filter(Objects::nonNull)
        .findFirst();

    matchingTrack.ifPresentOrElse(albumIdTrack -> {
      log.info("Found matching track with id: {}", albumIdTrack.right().getId());
      var spotifyIds = SpotifyIds.builder()
          .albumId(albumIdTrack.left())
          .trackId(albumIdTrack.right().getId())
          .build();
      audioFileDao.updateSpotifyIds(audioFile, spotifyIds);
    }, () -> log.info("Track matching name: '{}' and artist id: '{}' was not found.", trackName, artistId));

    return matchingTrack
        .map(Tuple::right)
        .map(t -> new SpotifyTrackId(t.getId(), t.getUri()))
        .orElse(null);
  }

  private SpotifyTrackId searchByArtistAndTrackNames(AudioFile audioFile) {
    var artistName = audioFile.artistName();
    var trackName = sanitizeTrackName(audioFile.trackName());
    var trackNameVariants = nameVariants(trackName);

    var spotifyResponse = (SpotifyResponse<Paging<Track>>) null;

    for (var trackNameVariant : trackNameVariants) {
      trackName = trackNameVariant;
      var searchQuery = "artist:" + artistName + " track:" + trackName;
      spotifyResponse = spotifyApiContext.execute(spotifyApi -> spotifyApi.searchTracks(searchQuery).build());
      if (spotifyResponse.hasError()) {
        log.error("Unable to search track by query.");
        return null;
      }
      if (spotifyResponse.getData().getTotal() > 0) {
        break;
      }
    }

    if (spotifyResponse == null) {
      return null;
    }

    var matchedTrack = Stream.of(spotifyResponse.getData().getItems())
        .filter(track -> {
          var audioFileAlbName = audioFile.albumName().toLowerCase();
          var spotifyAlbName = track.getAlbum().getName().toLowerCase();
          return spotifyAlbName.equals(audioFileAlbName)
              || audioFileAlbName.contains(spotifyAlbName)
              || spotifyAlbName.contains(audioFileAlbName);
        })
        .findFirst();

    var name = trackName;

    matchedTrack.ifPresentOrElse(
        track -> {
          var spotifyIds = SpotifyIds.builder()
              .artistId(track.getArtists()[0].getId())
              .albumId(track.getAlbum().getId())
              .trackId(track.getId())
              .build();
          audioFileDao.updateSpotifyIds(audioFile, spotifyIds);
        }, () -> log.info("Track matching name: '{}' and artist: '{}' was not found.", name, artistName));

    return matchedTrack.map(t -> new SpotifyTrackId(t.getId(), t.getUri())).orElse(null);
  }

  private String sanitizeTrackName(String value) {
    var openChar = '(';
    var closeChar = ')';
    var openIdx = value.indexOf(openChar);
    if (openIdx < 0) {
      openChar = '[';
      closeChar = ']';
    }
    openIdx = value.indexOf(openChar);
    if (openIdx < 0) {
      return value;
    }
    var closeIdx = value.indexOf(closeChar);
    if (closeIdx < 0) {
      return value;
    }

    if (closeIdx == value.length() - 1) {
      var sanitized = value.substring(0, openIdx - 1);

      log.warn("Input track: '{}', after sanitize: '{}'", value, sanitized);

      return sanitized.trim();
    }
    return value;
  }

  private List<String> nameVariants(String value) {
    if (value.contains(" & ")) {
      return List.of(value, value.replaceAll(" & ", " And "));
    }
    return List.of(value);
  }
}
