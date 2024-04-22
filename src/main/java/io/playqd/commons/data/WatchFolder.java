package io.playqd.commons.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.nio.file.Path;
import java.util.Set;

public record WatchFolder(long id,
                          String uuid,
                          String name,
                          @JsonIgnore
                          Path path,
                          boolean autoScanOnStartUp,
                          boolean watchable,
                          @JsonInclude(JsonInclude.Include.NON_EMPTY)
                          Set<String> ignoredDirectories) {

  @Override
  public String toString() {
    return String.format("id=%s; uuid:=%s; name=%s; path = %s; ignored location(s): %s; watchable: %s; autoScanOnStartUp: %s;",
        id(), uuid(), name(), path(), ignoredDirectories(), watchable(), autoScanOnStartUp());
  }
}