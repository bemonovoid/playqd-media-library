package io.playqd.commons.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.nio.file.Path;
import java.util.Map;

public record WatchFolderItem(String id,
                              String name,
                              @JsonIgnore
                              Path path,
                              String mimeType,
                              Long size,
                              ItemType itemType,
                              @JsonInclude(JsonInclude.Include.NON_EMPTY)
                              Map<ItemType, Long> childItemsCount) {

  @JsonIgnore
  public boolean hasChildren() {
    return childItemsCount != null && !childItemsCount.isEmpty();
  }

  @JsonIgnore
  public long totalChildItemsCount() {
    if (hasChildren()) {
      return childItemsCount.values().stream().mapToLong(Long::longValue).sum();
    } else {
      return 0;
    }
  }

  @JsonIgnore
  public long childFoldersCount() {
    if (hasChildren()) {
      return childItemsCount.getOrDefault(ItemType.folder, 0L);
    } else {
      return 0;
    }
  }
}