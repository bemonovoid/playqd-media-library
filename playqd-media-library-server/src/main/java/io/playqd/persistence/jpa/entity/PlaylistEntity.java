package io.playqd.persistence.jpa.entity;

import io.playqd.commons.data.PlaylistFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = PlaylistEntity.TABLE_NAME)
public class PlaylistEntity extends PersistableAuditableEntity {

  static final String TABLE_NAME = "playlists";

  private static final String COL_FORMAT = "format";
  private static final String COL_UUID = "uuid";
  private static final String COL_TITLE = "title";
  private static final String COL_FILE_NAME = "filename";
  private static final String COL_FILE_LOCATION = "location";
  private static final String COL_FILE_LAST_MODIFIED_DATE = "file_last_modified_date";

  @Column(name = COL_FORMAT)
  @Enumerated(EnumType.STRING)
  private PlaylistFormat format;

  @Column(name = COL_UUID)
  private String uuid;

  @Column(name = COL_TITLE, nullable = false)
  private String title;

  @Column(name = COL_FILE_NAME, nullable = false)
  private String fileName;

  @Column(name = COL_FILE_LOCATION, nullable = false)
  private String location;

  @Column(name = COL_FILE_LAST_MODIFIED_DATE, nullable = false)
  private LocalDateTime fileLastModifiedDate;
}