package io.playqd.controller;

import io.playqd.commons.data.Album;
import io.playqd.persistence.MediaLibraryDao;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/albums")
@Tag(name = "Albums")
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
  Page<Album> albums(@PageableDefault(size = PageDefaults.SIZE_25, sort = PageDefaults.SORT_BY_NAME) Pageable page,
                     @RequestParam(required = false) String artistId,
                     @RequestParam(required = false) String genreId) {
    if (StringUtils.hasLength(artistId)) {
      return mediaLibraryDao.ofAlbum().getByArtistId(artistId, page);
    }
    if (StringUtils.hasLength(genreId)) {
      return mediaLibraryDao.ofAlbum().getByGenreId(genreId, page);
    }
    return mediaLibraryDao.ofAlbum().getAll(page);
  }

}
