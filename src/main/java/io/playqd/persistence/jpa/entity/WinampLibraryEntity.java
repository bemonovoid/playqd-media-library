package io.playqd.persistence.jpa.entity;

import io.playqd.persistence.jpa.entity.PersistableAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = WinampLibraryEntity.TABLE_NAME)
public class WinampLibraryEntity extends PersistableAuditableEntity {

  static final String TABLE_NAME = "winamp_library";

  private static final String COL_DATA = "data";
  private static final String COL_FILE_NAME = "filename";
  private static final String COL_FILE_LOCATION = "location";
  private static final String COL_FILE_LAST_MODIFIED_DATE = "file_last_modified_date";

  @Column(name = COL_FILE_NAME, nullable = false)
  private String fileName;

  @Column(name = COL_FILE_LOCATION, nullable = false)
  private String location;

  @Lob
  @Column(name = COL_DATA, columnDefinition="LONGBLOB")
  private byte[] data;

  @Column(name = COL_FILE_LAST_MODIFIED_DATE, nullable = false)
  private LocalDateTime fileLastModifiedDate;
}
