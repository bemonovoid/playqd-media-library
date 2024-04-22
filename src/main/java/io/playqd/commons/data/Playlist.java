package io.playqd.commons.data;

import io.playqd.commons.data.PlaylistFormat;

import java.nio.file.Path;
import java.time.LocalDateTime;

public record Playlist(long id,
                       PlaylistFormat format,
                       String uuid,
                       String title,
                       String fileName,
                       Path location,
                       LocalDateTime fileLastModifiedDate) {
}
