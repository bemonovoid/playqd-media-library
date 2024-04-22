package io.playqd.persistence;

import io.playqd.persistence.GenreDao;

public interface MediaLibraryDao {

  ArtistDao ofArtist();

  AlbumDao ofAlbum();

  GenreDao ofGenre();

  AudioFileDao ofAudioFile();

}
