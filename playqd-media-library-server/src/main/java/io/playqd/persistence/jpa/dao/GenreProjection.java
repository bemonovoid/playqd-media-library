package io.playqd.persistence.jpa.dao;

public interface GenreProjection {

  String getId();

  String getName();

  int getArtists();

  int getAlbums();

  int getTracks();

}
