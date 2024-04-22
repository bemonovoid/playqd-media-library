package io.playqd.persistence.jpa.dao;

import io.playqd.persistence.AlbumDao;
import io.playqd.persistence.ArtistDao;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.GenreDao;
import io.playqd.persistence.MediaLibraryDao;
import org.springframework.stereotype.Component;

@Component
class CompositeMediaLibraryDao implements MediaLibraryDao {

  private final AlbumDao albumDao;
  private final GenreDao genreDao;
  private final ArtistDao artistDao;
  private final AudioFileDao audioFileDao;

  CompositeMediaLibraryDao(AlbumDao albumDao,
                                  GenreDao genreDao,
                                  ArtistDao artistDao,
                                  AudioFileDao audioFileDao) {
    this.albumDao = albumDao;
    this.genreDao = genreDao;
    this.artistDao = artistDao;
    this.audioFileDao = audioFileDao;
  }

  @Override
  public ArtistDao ofArtist() {
    return artistDao;
  }

  @Override
  public AlbumDao ofAlbum() {
    return albumDao;
  }

  @Override
  public GenreDao ofGenre() {
    return genreDao;
  }

  @Override
  public AudioFileDao ofAudioFile() {
    return audioFileDao;
  }
}
