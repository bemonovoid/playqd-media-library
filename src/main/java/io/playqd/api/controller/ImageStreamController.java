package io.playqd.api.controller;

import io.playqd.service.metadata.AlbumArtworkService;
import io.playqd.service.metadata.ImageSizeRequestParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping(RestApiResources.IMAGE)
class ImageStreamController {

  private final AlbumArtworkService albumArtworkService;

  ImageStreamController(AlbumArtworkService albumArtworkService) {
    this.albumArtworkService = albumArtworkService;
  }

  @GetMapping(path = "/albums/{albumId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
  ResponseEntity<byte[]> get(@PathVariable String albumId,
                             @RequestParam(required = false, name = "size") ImageSizeRequestParam size) {
    var mayBeAlbumArt = albumArtworkService.get(albumId);
    return mayBeAlbumArt
        .map(albumArt -> ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .body(albumArt.resources().getResizedOrOriginal(size).byteArray()))
        .orElseGet(() -> ResponseEntity
            .notFound()
            .build());
  }

  @GetMapping(
      path = "/albums/{albumId}/{albumFolderImageFileName}",
      produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
  ResponseEntity<byte[]> get(@PathVariable String albumId,
                             @PathVariable String albumFolderImageFileName,
                             @RequestParam(required = false, name = "size") ImageSizeRequestParam size) {
    var mayBeAlbumArt = albumArtworkService.get(albumId, albumFolderImageFileName);
    return mayBeAlbumArt
        .map(albumArt -> ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .body(albumArt.resources().getResizedOrOriginal(size).byteArray()))
        .orElseGet(() -> ResponseEntity
            .notFound()
            .build());
  }

}
