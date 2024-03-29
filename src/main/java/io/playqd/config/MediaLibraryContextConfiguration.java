package io.playqd.config;

import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.jpa.dao.JpaAudioFileDao;
import io.playqd.persistence.jpa.repository.AudioFileRepository;
import io.playqd.service.MetadataFileReader;
import io.playqd.service.metadata.FileAttributesToSqlParamsMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class MediaLibraryContextConfiguration {

  @Bean
  AudioFileDao audioFileDao(JdbcTemplate jdbcTemplate, AudioFileRepository audioFileRepository) {
    return new JpaAudioFileDao(jdbcTemplate, audioFileRepository);
  }

  @Bean
  FileAttributesToSqlParamsMapperFactory audioFileAttributesReaderFactory(MetadataFileReader metadataFileReader) {
    return new FileAttributesToSqlParamsMapperFactory(metadataFileReader);
  }
}