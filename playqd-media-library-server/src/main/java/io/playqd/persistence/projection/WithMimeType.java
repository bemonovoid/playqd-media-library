package io.playqd.persistence.projection;

public sealed interface WithMimeType extends AudioFileProjection permits AudioFileWithMimeType {

  String mimeType();
}
