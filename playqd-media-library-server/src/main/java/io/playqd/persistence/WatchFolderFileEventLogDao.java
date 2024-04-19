package io.playqd.persistence;

import java.time.LocalDate;

public interface WatchFolderFileEventLogDao {

  boolean hasEvents();

  LocalDate getLastAddedDate();
}
