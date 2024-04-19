package io.playqd.persistence;

import io.playqd.commons.data.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenreDao {

  long count();

  Page<Genre> getAll(Pageable pageable);

}