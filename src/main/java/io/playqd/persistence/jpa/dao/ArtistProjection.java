package io.playqd.persistence.jpa.dao;

public interface ArtistProjection {

  String getId();

  String getName();

  int getAlbums();

  int getTracks();
}
