package io.playqd.service;

import io.playqd.config.cache.CacheNames;
import io.playqd.exception.MetadataFileReaderException;
import io.playqd.service.metadata.MetadataFile;
import io.playqd.service.metadata.cue.CueFile;
import io.playqd.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.cache.annotation.CacheKey;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
class MetadataFileReaderImpl implements MetadataFileReader {

  @Override
  @Cacheable(cacheNames = CacheNames.METADATA_FILES)
  public MetadataFile read(@CacheKey String location) {
    var path = Paths.get(location);

    if (Files.isDirectory(path)) {
      throw new MetadataFileReaderException(String.format("Expected metadata file but was directory: '%s'", location));
    }

    var format = MetadataFileReader.Formats.fromFormat(FileUtils.getFileExtension(path))
        .orElseThrow(() -> new MetadataFileReaderException(String.format(
            "Metadata '%s' file is not supported. Supported formats: '%s'",
            location, Stream.of(Formats.values()).map(Formats::getFormat).collect(Collectors.joining(",")))));

    if (Formats.CUE == format) {
      var cueFile = new CueFile(path);
      log.info("Metadata file was successfully read and cached. {}", path);
      return cueFile;
    } else {
      throw new MetadataFileReaderException(
          String.format("'%s' format is supported but wasn't implemented, lazy man", format));
    }
  }

}
