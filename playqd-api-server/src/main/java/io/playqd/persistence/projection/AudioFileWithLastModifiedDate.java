package io.playqd.persistence.projection;

import java.time.Instant;

public record AudioFileWithLastModifiedDate(long id, String location, Instant fileLastModifiedDate)
    implements WithLastModifiedDate {
}
