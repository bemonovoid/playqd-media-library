package io.playqd.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spotify/callback")
@Tag(name = "Spotify")
public class SpotifyCallbackController {

  @GetMapping
  void accept(@RequestParam(name = "code") String code, @RequestParam(name = "state", required = false) String state) {

  }
}
