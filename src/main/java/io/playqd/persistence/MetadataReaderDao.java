package io.playqd.persistence;

import io.playqd.commons.data.Album;
import io.playqd.commons.data.Artist;
import io.playqd.commons.data.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MetadataReaderDao {

  long countArtists();

  long countAlbums();

  long countGenres();

  Page<Artist> getArtists(Pageable pageable);

  Page<Album> getAlbums(Pageable pageable);

  Page<Album> getAlbumsByArtistId(String artistId, Pageable pageable);

  Page<Album> getAlbumsByGenreId(String genreId, Pageable pageable);

  Page<Genre> getGenres(Pageable page);
}
