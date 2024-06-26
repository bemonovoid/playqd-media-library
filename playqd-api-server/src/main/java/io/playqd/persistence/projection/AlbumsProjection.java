package io.playqd.persistence.projection;

import java.time.LocalDate;

public interface AlbumsProjection {

  String getId();

  String getName();

  String getArtistId();

  String getArtistName();

  String getReleaseDate();

  String getGenreId();

  String getGenre();

  boolean getArtworkEmbedded();

  LocalDate getAddedToWatchFolderDate();

  int getTracks();
}
