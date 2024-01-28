package io.playqd.service.metadata;

import io.playqd.exception.AudioMetadataReadException;
import io.playqd.persistence.jpa.entity.AudioFileJpaEntity;
import io.playqd.persistence.jpa.entity.AuditableEntity;
import io.playqd.util.FileUtils;
import io.playqd.util.TimeUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class CommonFileAttributesToSqlParamsMapper implements FileAttributesToSqlParamsMapper {

  @Override
  public Map<String, Object> toSqlParams(Path path) {
    try {
      var file = path.toFile();
      var fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
      var fileNameAndExtension = FileUtils.getFileNameAndExtension(file.getName());

      var params = new LinkedHashMap<String, Object>();

      // File attributes
      params.put(AudioFileJpaEntity.COL_SIZE, fileAttributes.size());
      params.put(AudioFileJpaEntity.COL_NAME, fileNameAndExtension.left());
      params.put(AudioFileJpaEntity.COL_LOCATION, path.toString());
      params.put(AudioFileJpaEntity.COL_EXTENSION, fileNameAndExtension.right());
      params.put(AudioFileJpaEntity.COL_MIME_TYPE, FileUtils.detectMimeType(path));
      params.put(AudioFileJpaEntity.COL_FILE_LAST_SCANNED_DATE, Instant.now());

      params.put(AudioFileJpaEntity.COL_FILE_LAST_MODIFIED_DATE,
          TimeUtils.millisToInstant(fileAttributes.lastModifiedTime().toMillis()));

      params.put(AudioFileJpaEntity.COL_FILE_ADDED_TO_WATCH_FOLDER_DATE,
          TimeUtils.millisToLocalDate(fileAttributes.creationTime().toMillis()));

      // Audit
      params.put(AuditableEntity.COL_CREATED_BY, "system");
      params.put(AuditableEntity.COL_CREATED_DATE, Instant.now());

      return params;
    } catch (IOException e) {
      throw new AudioMetadataReadException(e);
    }

  }
}
