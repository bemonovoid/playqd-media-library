package io.playqd.persistence;

import io.playqd.commons.data.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ArtistDao {

  long count();

  Optional<Artist> getById(String artistId);

  Page<Artist> getAll(Pageable pageable);

  Page<Artist> getByGenreId(String genreId, Pageable pageable);

}
