package io.playqd.controller;

import io.playqd.commons.data.Album;
import io.playqd.commons.data.AlbumQueryParams;
import io.playqd.persistence.MediaLibraryDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/albums")
class AlbumController {

  private final MediaLibraryDao mediaLibraryDao;

  AlbumController(MediaLibraryDao mediaLibraryDao) {
    this.mediaLibraryDao = mediaLibraryDao;
  }

  @GetMapping("/{albumId}")
  ResponseEntity<Album> album(@PathVariable String albumId) {
    return ResponseEntity.of(mediaLibraryDao.ofAlbum().getById(albumId));
  }

  @GetMapping
  Page<Album> albums(@PageableDefault(size = 100, sort = "name") Pageable page, AlbumQueryParams params) {
    if (StringUtils.hasLength(params.artistId())) {
      return mediaLibraryDao.ofAlbum().getByArtistId(params.artistId(), page);
    }
    if (StringUtils.hasLength(params.genreId())) {
      return mediaLibraryDao.ofAlbum().getByGenreId(params.genreId(), page);
    }
    return mediaLibraryDao.ofAlbum().getAll(page);
  }

}
