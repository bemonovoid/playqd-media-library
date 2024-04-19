package io.playqd.commons.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;

public record Album(String id,
                    String name,
                    String releaseDate,
                    String genreId,
                    String genre,
                    String artistId,
                    String artistName,
                    boolean artworkEmbedded,
                    @JsonIgnore
                    LocalDate addedToWatchFolderDate,
                    int tracksCount) {

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof io.playqd.commons.data.Album) {
      return id().equals(((io.playqd.commons.data.Album) obj).id());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return id().hashCode();
  }

}
