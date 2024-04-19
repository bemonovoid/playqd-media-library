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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
class MetadataFileReaderImpl implements MetadataFileReader {

  @Override
  @Cacheable(cacheNames = CacheNames.METADATA_FILES)
  public MetadataFile findAndReadCueSheetFromDir(@CacheKey String dir) {

    var cf = CompletableFuture.supplyAsync(() -> {
      try (var files = Files.list(Paths.get(dir))) {

        var cueFiles = files
            .filter(filePath -> MetadataFileReader.Formats.CUE.getFormat().equals(FileUtils.getFileExtension(filePath)))
            .toList();

        if (cueFiles.isEmpty()) {
          return MetadataFile.emptyFile();
        }

        if (cueFiles.size() > 1) {
          // log matching files
          return MetadataFile.emptyFile();
        }

        return read(cueFiles.get(0));

      } catch (IOException e) {
        return MetadataFile.emptyFile();
      }
    }, Executors.newVirtualThreadPerTaskExecutor());

    try {
      return cf.get();
    } catch (Exception e) {
      log.error("", e);
      return MetadataFile.emptyFile();
    }

  }

  private MetadataFile read(Path path) {
    if (Files.isDirectory(path)) {
      throw new MetadataFileReaderException(String.format("Expected metadata file but was directory: '%s'", path));
    }

    var format = Formats.fromFormat(FileUtils.getFileExtension(path))
        .orElseThrow(() -> new MetadataFileReaderException(String.format(
            "Metadata '%s' file is not supported. Supported formats: '%s'",
            path, Stream.of(Formats.values()).map(Formats::getFormat).collect(Collectors.joining(",")))));

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