package io.playqd.controller;

import io.playqd.commons.data.Artist;
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
@RequestMapping("/api/v1/artists")
@Tag(name = "Artists")
class ArtistController {

  private final MediaLibraryDao mediaLibraryDao;

  ArtistController(MediaLibraryDao mediaLibraryDao) {
    this.mediaLibraryDao = mediaLibraryDao;
  }

  @GetMapping("/{artistId}")
  ResponseEntity<Artist> artist(@PathVariable String artistId) {
    return ResponseEntity.of(mediaLibraryDao.ofArtist().getById(artistId));
  }

  @GetMapping
  Page<Artist> artists(@PageableDefault(size = PageDefaults.SIZE_25, sort = PageDefaults.SORT_BY_NAME) Pageable page,
                       @RequestParam(required = false) String genreId) {
    if (StringUtils.hasLength(genreId)) {
      return mediaLibraryDao.ofArtist().getByGenreId(genreId, page);
    }
    return mediaLibraryDao.ofArtist().getAll(page);
  }

}
