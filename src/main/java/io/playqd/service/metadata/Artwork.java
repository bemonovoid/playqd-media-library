package io.playqd.service.metadata;

import java.io.Serializable;

public record Artwork(long size,
                      String mimeType,
                      byte[] binaryData) implements Serializable {
}