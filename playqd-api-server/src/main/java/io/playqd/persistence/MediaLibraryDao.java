package io.playqd.persistence;

public interface MediaLibraryDao {

  ArtistDao ofArtist();

  AlbumDao ofAlbum();

  GenreDao ofGenre();

  AudioFileDao ofAudioFile();

}
