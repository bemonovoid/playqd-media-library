package io.playqd.service.metadata;

import io.playqd.service.MetadataFileReader;
import io.playqd.service.jtagger.JTaggerAudioFileAttributesToDatabaseParamsMapper;

public final class FileAttributesToSqlParamsMapperFactory {

  private final MetadataFileReader metadataFileReader;

  public FileAttributesToSqlParamsMapperFactory(MetadataFileReader metadataFileReader) {
    this.metadataFileReader = metadataFileReader;
  }

  public FileAttributesToSqlParamsMapper get() {
    return new JTaggerAudioFileAttributesToDatabaseParamsMapper(metadataFileReader);
  }

}
