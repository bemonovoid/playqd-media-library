package io.playqd.model.event;

import io.playqd.commons.data.WatchFolder;

import java.nio.file.Path;
import java.util.Set;

public record WatchFolderModifiedEvent(WatchFolder watchFolder, Set<Path> changedContentDirs) {
}
