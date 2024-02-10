package io.playqd.api.controller;

import io.playqd.commons.data.ArtworkSize;
import io.playqd.service.MusicDirectoryPathResolver;
import io.playqd.service.metadata.AlbumArtworkService;
import io.playqd.service.metadata.ArtworkKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping(RestApiResources.IMAGE)
class ImageStreamController {

  private final AlbumArtworkService albumArtworkService;
  private final MusicDirectoryPathResolver musicDirectoryPathResolver;

  ImageStreamController(AlbumArtworkService albumArtworkService,
                        MusicDirectoryPathResolver musicDirectoryPathResolver) {
    this.albumArtworkService = albumArtworkService;
    this.musicDirectoryPathResolver = musicDirectoryPathResolver;
  }

  @GetMapping(path = "/{locationBase64Encoded}")
  ResponseEntity<byte[]> getFromLocation(@PathVariable(name = "locationBase64Encoded") String locationBase64Encoded) {
    try {
      var imageLocationDecoded = new String(Base64.getDecoder().decode(locationBase64Encoded));
      var imagePath = musicDirectoryPathResolver.unRelativize(Paths.get(imageLocationDecoded));

      if (!Files.exists(imagePath)) {
        log.warn("Image file: {} wasn't found.", imagePath);
        return ResponseEntity.notFound().build();
      }

      if (Files.isDirectory(imagePath)) {
        log.warn("Image file was a directory. {}", imagePath);
        return ResponseEntity.badRequest().build();
      }

      return ResponseEntity
          .ok()
          .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
          .body(Files.readAllBytes(imagePath));
    } catch (Exception e) {
      log.error("", e);
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping(path = "/albums/{albumId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
  ResponseEntity<byte[]> get(@PathVariable(name = "albumId") String albumId,
                             @RequestParam(name = "size", defaultValue = "original", required = false) ArtworkSize size) {
    var mayBeAlbumArt = albumArtworkService.getArtwork(new ArtworkKey(albumId, size));
    return mayBeAlbumArt
        .map(albumArt -> ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
            .body(albumArt.binaryData()))
        .orElseGet(() -> ResponseEntity
            .notFound()
            .build());
  }
}