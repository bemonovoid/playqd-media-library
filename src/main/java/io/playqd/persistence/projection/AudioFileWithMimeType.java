package io.playqd.persistence.projection;

public record AudioFileWithMimeType(long id, String location, String mimeType) implements WithMimeType {
}
