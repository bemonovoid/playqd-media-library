package io.playqd.controller;

import io.playqd.commons.data.Genre;
import io.playqd.persistence.MediaLibraryDao;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/genres")
@Tag(name = "Genres")
class GenreController {

  private final MediaLibraryDao mediaLibraryDao;

  GenreController(MediaLibraryDao mediaLibraryDao) {
    this.mediaLibraryDao = mediaLibraryDao;
  }

  @GetMapping
  Page<Genre> genres(@PageableDefault(size = PageDefaults.SIZE_25, sort = PageDefaults.SORT_BY_NAME) Pageable page) {
    return mediaLibraryDao.ofGenre().getAll(page);
  }
}