package io.playqd.service.metadata;

import java.io.Serializable;

public record ImageResource(String uri, byte[] byteArray) implements Serializable {
}
