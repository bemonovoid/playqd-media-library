package io.playqd.controller;

import io.playqd.commons.data.ArtworkSize;
import io.playqd.commons.data.ItemType;
import io.playqd.service.metadata.AlbumArtworkService;
import io.playqd.service.metadata.ArtworkKey;
import io.playqd.service.watchfolder.WatchFolderBrowser;
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
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/v1/artwork")
class ArtworkController {

  private final WatchFolderBrowser watchFolderBrowser;
  private final AlbumArtworkService albumArtworkService;

  ArtworkController(WatchFolderBrowser watchFolderBrowser, AlbumArtworkService albumArtworkService) {
    this.watchFolderBrowser = watchFolderBrowser;
    this.albumArtworkService = albumArtworkService;
  }

  @GetMapping(path = "/{imageId}")
  ResponseEntity<byte[]> getFromLocation(@PathVariable(name = "imageId") String imageId) {
    try {
      var watchFolderItemOpt = watchFolderBrowser.get(imageId);
      if (watchFolderItemOpt.isEmpty()) {
        log.warn("No image with id: '{}' found.", imageId);
        return ResponseEntity.notFound().build();
      }
      var watchFolderItem = watchFolderItemOpt.get();
      if (ItemType.imageFile != watchFolderItem.itemType()) {
        log.warn("Resource with id: '{}' isn't an image.", imageId);
        return ResponseEntity.notFound().build();
      }

      var imagePath = watchFolderItem.path();

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