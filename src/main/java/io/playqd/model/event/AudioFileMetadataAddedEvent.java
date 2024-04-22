package io.playqd.model.event;

import java.time.LocalDate;

public record AudioFileMetadataAddedEvent(int total, LocalDate addedToWatchFolderDate) {

}
