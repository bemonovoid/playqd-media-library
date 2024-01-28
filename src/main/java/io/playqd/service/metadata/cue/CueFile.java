package io.playqd.service.metadata.cue;

import io.playqd.service.metadata.MetadataFile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public final class CueFile implements MetadataFile {

  private static final String ATTR_PERFORMER = "PERFORMER";
  private static final String ATTR_TITLE = "TITLE";
  private static final String ATTR_REM_DATE = "REM DATE";

  private static final List<Charset> CHARSETS = List.of(UTF_8, Charset.forName("KOI8-U"), UTF_16);

  private final Map<String, String> attributes;

  public CueFile(Path cueFileDir) {
    attributes = parseToAttributeMap(cueFileDir, new LinkedList<>(CHARSETS));
  }

  private static Map<String, String> parseToAttributeMap(Path cueFile, Queue<Charset> charsets) {

    var parsedAttributes = new HashMap<String, String>();

    var charset = charsets.poll();

    log.info("Parsing metadata from '{}'. Using charset: {}", cueFile, charset.name());

    try (Stream<String> lines = Files.lines(cueFile, charset)) {

      lines.forEach(line -> {
        if (line.startsWith(ATTR_REM_DATE)) {
          var value = line.substring(ATTR_REM_DATE.length() + 1);
          parsedAttributes.putIfAbsent(ATTR_REM_DATE, StringUtils.unwrap(value, '"'));
        } else if (line.startsWith("REM COMMENT")) {
          log.warn("'REM COMMENT' field detected. Skipping for now ...");
        } else if (line.startsWith(ATTR_PERFORMER)) {
          var value = line.substring(ATTR_PERFORMER.length() + 1);
          parsedAttributes.putIfAbsent(ATTR_PERFORMER, StringUtils.unwrap(value, '"'));
        } else if (line.startsWith(ATTR_TITLE)) {
          var value = line.substring(ATTR_TITLE.length() + 1);
          parsedAttributes.putIfAbsent(ATTR_TITLE, StringUtils.unwrap(value, '"'));
        }
      });
    } catch (IOException e) {
      log.error("Error reading '{}'. Reason: {}", cueFile, e.getMessage());
      return Collections.emptyMap();
    } catch (Exception e) {
      log.error("Error parsing '{}'. Charset: {}. Reason: {}", cueFile, charset.name(), e.getMessage());
      if (!charsets.isEmpty()) {
        return parseToAttributeMap(cueFile, charsets);
      }
    }

    return parsedAttributes;

  }

  @Override
  public Optional<String> performer() {
    return Optional.ofNullable(attributes.get(ATTR_PERFORMER));
  }

  @Override
  public Optional<String> title() {
    return Optional.ofNullable(attributes.get(ATTR_TITLE));
  }

  @Override
  public Optional<String> remDate() {
    return Optional.ofNullable(attributes.get(ATTR_REM_DATE));
  }

}
