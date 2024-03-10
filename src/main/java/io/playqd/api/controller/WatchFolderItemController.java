package io.playqd.api.controller;

import io.playqd.commons.data.WatchFolderItem;
import io.playqd.service.watchfolder.WatchFolderBrowser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/folders")
class WatchFolderItemController {

  private final WatchFolderBrowser watchFolderBrowser;

  WatchFolderItemController(WatchFolderBrowser watchFolderBrowser) {
    this.watchFolderBrowser = watchFolderBrowser;
  }

  @GetMapping("/items")
  List<WatchFolderItem> watchFolderItems() {
    return watchFolderBrowser.browse();
  }

  @GetMapping("/items/{folderId}")
  Page<WatchFolderItem> watchFolderItems(@PathVariable("folderId") String folderId,
                                         @PageableDefault(size = 1000) Pageable page) {
    return watchFolderBrowser.browse(folderId, page);
  }
}