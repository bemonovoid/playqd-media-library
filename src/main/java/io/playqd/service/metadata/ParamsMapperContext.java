package io.playqd.service.metadata;

import io.playqd.commons.data.WatchFolder;

import java.nio.file.Path;

public record ParamsMapperContext(WatchFolder musicDirectory, Path path) {
}
