package io.playqd.service.winamp;

import io.playqd.service.winamp.nde.NdeData;
import lombok.Builder;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Builder
public record WinampLibrary(long id, String fileName, Path location, NdeData data, LocalDateTime fileLastModifiedDate) {

  public boolean isNew() {
    return id() < 0;
  }
}
