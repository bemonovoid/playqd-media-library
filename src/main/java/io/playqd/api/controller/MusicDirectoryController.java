package io.playqd.api.controller;

import io.playqd.commons.data.MusicDirectory;
import io.playqd.commons.data.MusicDirectoryAction;
import io.playqd.commons.data.MusicDirectoryActionVisitor;
import io.playqd.commons.data.MusicDirectoryContentInfo;
import io.playqd.service.mediasource.MusicDirectoryManager;
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
@RequestMapping("/api/v1/directories")
class MusicDirectoryController {

  private final MusicDirectoryManager musicDirectoryManager;
  private final MusicDirectoryActionVisitor musicDirectoryActionVisitor;

  MusicDirectoryController(MusicDirectoryManager musicDirectoryManager,
                           MusicDirectoryActionVisitor musicDirectoryActionVisitor) {
    this.musicDirectoryManager = musicDirectoryManager;
    this.musicDirectoryActionVisitor = musicDirectoryActionVisitor;
  }

  @GetMapping("/{id}")
  MusicDirectory get(@PathVariable long id) {
    return musicDirectoryManager.get(id);
  }

  @GetMapping
  List<MusicDirectory> getAll() {
    return musicDirectoryManager.getAll();
  }

  @GetMapping("/{id}/info")
  MusicDirectoryContentInfo info(@PathVariable long id) {
    return musicDirectoryManager.info(id);
  }

  @PostMapping("/actions")
  @ResponseStatus(HttpStatus.ACCEPTED)
  void scan(@RequestBody MusicDirectoryAction action) {
    action.accept(musicDirectoryActionVisitor);
  }
}
