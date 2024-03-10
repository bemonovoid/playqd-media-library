package io.playqd.api.controller;

import io.playqd.commons.data.WatchFolder;
import io.playqd.commons.data.WatchFolderAction;
import io.playqd.commons.data.WatchFolderActionVisitor;
import io.playqd.commons.data.WatchFolderContentInfo;
import io.playqd.persistence.WatchFolderDao;
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
  private final WatchFolderActionVisitor watchFolderActionVisitor;

  WatchFolderController(WatchFolderDao watchFolderDao,
                        WatchFolderActionVisitor watchFolderActionVisitor) {
    this.watchFolderDao = watchFolderDao;
    this.watchFolderActionVisitor = watchFolderActionVisitor;
  }

  @GetMapping("/{id}")
  WatchFolder get(@PathVariable("id") long id) {
    return watchFolderDao.get(id);
  }

  @GetMapping
  List<WatchFolder> getAll() {
    return watchFolderDao.getAll();
  }

  @GetMapping("/{id}/info")
  WatchFolderContentInfo info(@PathVariable long id) {
    return null;
  }

  @PostMapping("/actions")
  @ResponseStatus(HttpStatus.ACCEPTED)
  void scan(@RequestBody WatchFolderAction action) {
    action.accept(watchFolderActionVisitor);
  }
}