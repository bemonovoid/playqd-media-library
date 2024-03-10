package io.playqd.service.metadata;

import io.playqd.exception.AudioMetadataReadException;
import io.playqd.persistence.jpa.entity.AudioFileJpaEntity;
import io.playqd.persistence.jpa.entity.AuditableEntity;
import io.playqd.service.WatchFolderFilePathResolver;
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

  protected final WatchFolderFilePathResolver watchFolderFilePathResolver;

  protected CommonFileAttributesToSqlParamsMapper(WatchFolderFilePathResolver watchFolderFilePathResolver) {
    this.watchFolderFilePathResolver = watchFolderFilePathResolver;
  }

  @Override
  public Map<String, Object> toSqlParams(ParamsMapperContext context) {
    var path = context.path();
    try {
      var file = path.toFile();
      var fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
      var fileNameAndExtension = FileUtils.getFileNameAndExtension(file.getName());

      var params = new LinkedHashMap<String, Object>();

      // File attributes
      params.put(AudioFileJpaEntity.COL_SIZE, fileAttributes.size());
      params.put(AudioFileJpaEntity.COL_NAME, fileNameAndExtension.left());
      params.put(AudioFileJpaEntity.COL_EXTENSION, fileNameAndExtension.right());
      params.put(AudioFileJpaEntity.COL_MIME_TYPE, FileUtils.detectMimeType(path));
      params.put(AudioFileJpaEntity.COL_SOURCE_DIR_ID, context.musicDirectory().id());
      params.put(AudioFileJpaEntity.COL_FILE_LAST_SCANNED_DATE, Instant.now());

      params.put(AudioFileJpaEntity.COL_LOCATION,
          watchFolderFilePathResolver.relativize(context.musicDirectory().path(), path));

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

  public static void main(String[] args) {
    Path p = Path.of("Apparat\\Apparat - Tttrial and Eror\\06. ABS.flac");
    Path p2 = Path.of("Apparat/Apparat - Tttrial and Eror/06. ABS.flac");
    System.out.println(p);
    System.out.println(p2);
  }
}
