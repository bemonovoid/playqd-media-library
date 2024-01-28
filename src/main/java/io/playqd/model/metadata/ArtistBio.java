package io.playqd.model.metadata;

import java.io.Serializable;

public record ArtistBio(String summary, String content) implements Serializable {
}
