package io.playqd.persistence.jpa.repository;

import io.playqd.persistence.jpa.entity.view.AlbumViewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AlbumViewRepository extends MediaItemViewRepository<AlbumViewEntity> {

  Page<AlbumViewEntity> findAllByArtistId(String artistId, Pageable pageable);

  Page<AlbumViewEntity> findAllByGenreId(String genreId, Pageable pageable);
}
