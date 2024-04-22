package io.playqd.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;

public interface AudioFile {

  Long id();

  String artistId();

  String albumId();

  String trackId();

  String genreId();

  long size();

  String name();

  String location();

  default Path path() {
    return Paths.get(location());
  }

  String extension();

  String mimeType();

  int playbackCount();

  Instant fileLastPlaybackDate();

  Instant fileLastScannedDate();

  Instant fileLastModifiedDate();

  LocalDate fileAddedToWatchFolderDate();

  String format();

  String bitRate();

  String channels();

  boolean lossless();

  String sampleRate();

  String encodingType();

  int bitsPerSample();

  String artistName();

  String artistCountry();

  String albumName();

  String albumReleaseDate();

  String trackName();

  String trackNumber();

  int trackLength();

  double preciseTrackLength();

  String comment();

  String lyrics();

  String genre();

  String rating();

  boolean artworkEmbedded();

  String mbTrackId();

  String mbArtistId();

  String mbReleaseType();

  String mbReleaseGroupId();

  String spotifyArtistId();

  String spotifyAlbumId();

  String spotifyTrackId();

}
