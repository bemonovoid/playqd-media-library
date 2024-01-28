package io.playqd.model.event;

import io.playqd.commons.data.MusicDirectory;

import java.nio.file.Path;
import java.util.Set;

public record MusicDirectoryContentChangedEvent(MusicDirectory musicDirectory, Set<Path> changedContentDirs) {
}
