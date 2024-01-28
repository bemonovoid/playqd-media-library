package io.playqd.service.metadata;

import java.io.Serializable;

public record Artwork(String albumId, ImageResources resources, ImageMetadata metadata) implements Serializable {

}
