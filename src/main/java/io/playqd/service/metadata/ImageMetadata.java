package io.playqd.service.metadata;

import java.io.Serializable;

public record ImageMetadata(long size,
                            String mimeType,
                            Dimensions dimensions) implements Serializable {
}
