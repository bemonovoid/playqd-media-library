package io.playqd.controller;

import io.playqd.commons.data.WatchFolder;
import io.playqd.commons.data.WatchFolderAction;
import io.playqd.commons.data.WatchFolderActionVisitor;
import io.playqd.commons.data.WatchFolderContentInfo;
import io.playqd.commons.data.WatchFolderItem;
import io.playqd.persistence.WatchFolderDao;
import io.playqd.service.watchfolder.WatchFolderBrowser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/folders")
class WatchFolderController {

  private final WatchFolderDao watchFolderDao;
  private final WatchFolderBrowser watchFolderBrowser;
  private final WatchFolderActionVisitor watchFolderActionVisitor;

  WatchFolderController(WatchFolderDao watchFolderDao,
                        WatchFolderBrowser watchFolderBrowser,
                        WatchFolderActionVisitor watchFolderActionVisitor) {
    this.watchFolderDao = watchFolderDao;
    this.watchFolderBrowser = watchFolderBrowser;
    this.watchFolderActionVisitor = watchFolderActionVisitor;
  }

  @GetMapping
  List<WatchFolder> getAll() {
    return watchFolderDao.getAll();
  }

  @GetMapping("/{id}")
  WatchFolder get(@PathVariable("id") long id) {
    return watchFolderDao.get(id);
  }

  @GetMapping("/{id}/content")
  WatchFolderContentInfo info(@PathVariable long id) {
    return null;
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

  @PostMapping("/actions")
  @ResponseStatus(HttpStatus.ACCEPTED)
  void scan(@RequestBody WatchFolderAction action) {
    action.accept(watchFolderActionVisitor);
  }
}