package io.playqd.service.jtagger;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public final class JTaggerAudioFileUtils {

  private static final List<FieldKey> ARTIST_NAME_TAGS = List.of(
      FieldKey.ARTIST, FieldKey.ALBUM_ARTIST, FieldKey.ORIGINAL_ARTIST, FieldKey.COMPOSER);

  private JTaggerAudioFileUtils() {

  }

  public static AudioFile read(Path path) {
    try {
      return AudioFileIO.read(path.toFile());
    } catch (CannotReadException | TagException | InvalidAudioFrameException | ReadOnlyFileException | IOException e) {
      log.error("AudioFile read error. {}", path, e);
      return null;
    }
  }

  public static String readFromTag(AudioFile audioFile, FieldKey key) {
    return readFromTag(audioFile, key, () -> null);
  }

  public static String readFromTag(AudioFile audioFile, FieldKey key, Supplier<String> defaultValue) {
    try {
      return Optional.ofNullable(audioFile.getTag())
          .map(tag -> tag.getFirst(key))
          .map(String::trim)
          .filter(StringUtils::hasText)
          .orElseGet(defaultValue);
    } catch (Exception e) { // UnsupportedOperationException | KeyNotFoundException
      log.error("Failed to read tag: '{}' from:  {}. Error details: {}",
          key.name(), audioFile.getFile().getAbsolutePath(), e.getMessage());
      return null;
    }
  }

  static List<FieldKey> getArtistNameTagsOrdered() {
    return ARTIST_NAME_TAGS;
  }
}
