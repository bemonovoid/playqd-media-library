package io.playqd.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = WatchFolderLastFileEventLogJpaEntity.TABLE_NAME)
public class WatchFolderLastFileEventLogJpaEntity extends PersistableAuditableEntity {

  public static final String TABLE_NAME = "watch_folder_last_file_event_log";

  private static final String COL_WATCH_FOLDER_LAST_ADDED_DATE = "watch_folder_file_last_added_date";

  private static final String COL_WATCH_FOLDER_LAST_MODIFIED_DATE = "watch_folder_file_last_modified_date";

  @Column(name = COL_WATCH_FOLDER_LAST_ADDED_DATE)
  private LocalDate lastAddedDate;

}
