package io.playqd.commons.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public record WatchFolderContentInfo(@JsonIgnore WatchFolder watchFolder,
                                     long totalCount,
                                     Map<String, Long> formats) {
}