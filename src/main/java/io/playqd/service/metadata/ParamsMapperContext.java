package io.playqd.service.metadata;

import io.playqd.commons.data.MusicDirectory;

import java.nio.file.Path;

public record ParamsMapperContext(MusicDirectory musicDirectory, Path path) {
}
