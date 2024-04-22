package io.playqd.persistence;

import io.playqd.commons.data.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface AlbumDao {

  long count();

  Optional<Album> getById(String albumId);

  Page<Album> getAll(Pageable pageable);

  Page<Album> getByArtistId(String artistId, Pageable pageable);

  Page<Album> getByGenreId(String genreId, Pageable pageable);

}
