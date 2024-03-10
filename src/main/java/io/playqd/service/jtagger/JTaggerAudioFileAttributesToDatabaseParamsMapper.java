package io.playqd.service.jtagger;

import io.playqd.exception.AudioMetadataReadException;
import io.playqd.persistence.jpa.entity.AudioFileJpaEntity;
import io.playqd.service.MetadataFileReader;
import io.playqd.service.metadata.CommonFileAttributesToSqlParamsMapper;
import io.playqd.service.metadata.MetadataFile;
import io.playqd.service.metadata.ParamsMapperContext;
import io.playqd.util.FileUtils;
import io.playqd.util.UUIDV3Ids;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class JTaggerAudioFileAttributesToDatabaseParamsMapper extends CommonFileAttributesToSqlParamsMapper {

  private static final String UNKNOWN_ARTIST = "unknown_artist";
  private static final String UNKNOWN_ALBUM = "unknown_album";
  private static final String UNKNOWN_GENRE = "unknown_genre";

  private final Map<String, String> UUIDS = new ConcurrentHashMap<>();

  private final MetadataFileReader metadataFileReader;

  public JTaggerAudioFileAttributesToDatabaseParamsMapper(MetadataFileReader metadataFileReader) {
    this.metadataFileReader = metadataFileReader;
  }

  private static void applyArtworkDetails(AudioFile audioFile, Consumer<Artwork> artworkConsumer) {
    Tag tag = audioFile.getTag();
    if (tag != null && tag.getFirstArtwork() != null) {
      artworkConsumer.accept(tag.getFirstArtwork());
    }
  }

  /**
   * @param context to read audio metadata from
   * @return
   * @throws AudioMetadataReadException
   */
  public Map<String, Object> toSqlParams(ParamsMapperContext context) {
    var path = context.path();
    try {
      var file = path.toFile();
      var jTaggerAudioFile = AudioFileIO.read(file);
      var fileName = FileUtils.getFileNameWithoutExtension(file.getName());

      var params = new LinkedHashMap<>(super.toSqlParams(context));

      // Audio
      params.put(AudioFileJpaEntity.COL_FORMAT, jTaggerAudioFile.getAudioHeader().getFormat());
      params.put(AudioFileJpaEntity.COL_BIT_RATE, jTaggerAudioFile.getAudioHeader().getBitRate());
      params.put(AudioFileJpaEntity.COL_CHANNELS, jTaggerAudioFile.getAudioHeader().getChannels());
      params.put(AudioFileJpaEntity.COL_LOSSLESS, jTaggerAudioFile.getAudioHeader().isLossless());
      params.put(AudioFileJpaEntity.COL_SAMPLE_RATE, jTaggerAudioFile.getAudioHeader().getSampleRate());
      params.put(AudioFileJpaEntity.COL_ENCODING_TYPE, jTaggerAudioFile.getAudioHeader().getEncodingType());
      params.put(AudioFileJpaEntity.COL_BITS_PER_SAMPLE, jTaggerAudioFile.getAudioHeader().getBitsPerSample());

      // Artist
      var artistName = getArtistName(jTaggerAudioFile);
      var artistId = UUIDS.computeIfAbsent(artistName, UUIDV3Ids::create);

      params.put(AudioFileJpaEntity.COL_ARTIST_NAME, artistName);
      params.put(AudioFileJpaEntity.COL_ARTIST_ID, artistId);
      params.put(AudioFileJpaEntity.COL_ARTIST_COUNTRY,
          AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.COUNTRY));

      // Album
      var albumName = getAlbumName(jTaggerAudioFile);
      var albumId = UUIDS.computeIfAbsent(artistName + albumName, UUIDV3Ids::create);
      params.put(AudioFileJpaEntity.COL_ALBUM_NAME, albumName);
      params.put(AudioFileJpaEntity.COL_ALBUM_ID, albumId);
      params.put(AudioFileJpaEntity.COL_ALBUM_RELEASE_DATE, getAlbumReleaseDate(jTaggerAudioFile));

      // Track
      var trackName = AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.TITLE, () -> fileName);
      params.put(AudioFileJpaEntity.COL_TRACK_NAME, trackName);
      params.put(AudioFileJpaEntity.COL_TRACK_ID, UUIDV3Ids.create(file.getPath()));
      params.put(AudioFileJpaEntity.COL_TRACK_NUMBER,
          AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.TRACK));
      params.put(AudioFileJpaEntity.COL_TRACK_LENGTH, jTaggerAudioFile.getAudioHeader().getTrackLength());
      params.put(AudioFileJpaEntity.COL_PRECISE_TRACK_LENGTH,
          jTaggerAudioFile.getAudioHeader().getPreciseTrackLength());

      // Genre
      var genreName = AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.GENRE, () -> UNKNOWN_GENRE);
      var genreId = UUIDS.computeIfAbsent("genre:" + genreName, UUIDV3Ids::create);
      params.put(AudioFileJpaEntity.COL_GENRE, genreName);
      params.put(AudioFileJpaEntity.COL_GENRE_ID, genreId);

      // Artwork
      params.put(AudioFileJpaEntity.COL_ARTWORK_EMBEDDED, false);
      applyArtworkDetails(jTaggerAudioFile, artwork -> {
        if (artwork.getBinaryData() != null && artwork.getBinaryData().length > 0) {
          params.put(AudioFileJpaEntity.COL_ARTWORK_EMBEDDED, true);
        }
      });

      // Other
      params.put(AudioFileJpaEntity.COL_COMMENT,
          AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.COMMENT));
      params.put(AudioFileJpaEntity.COL_LYRICS,
          AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.LYRICS));

      // Musicbrainz
      params.put(AudioFileJpaEntity.COL_MB_ARTIST_ID,
          AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.MUSICBRAINZ_ARTISTID));
      params.put(AudioFileJpaEntity.COL_MB_TRACK_ID,
          AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.MUSICBRAINZ_TRACK_ID));
      params.put(AudioFileJpaEntity.COL_MB_RELEASE_TYPE,
          AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.MUSICBRAINZ_RELEASE_TYPE));
      params.put(AudioFileJpaEntity.COL_MB_RELEASE_GROUP_ID,
          AudioFileTagReader.readFromTag(jTaggerAudioFile, FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID));

      // Defaults
      params.put(AudioFileJpaEntity.COL_FILE_PLAYBACK_COUNT, 0);

      return params;
    } catch (Exception e) {
      throw new AudioMetadataReadException(e);
    }
  }

  private String getArtistName(AudioFile audioFile) {
    return AudioFileTagReader.getArtistNameTagsOrdered().stream()
        .map(fieldKey -> AudioFileTagReader.readFromTag(audioFile, fieldKey))
        .filter(StringUtils::hasText)
        .findFirst()
        .or(() -> readAlbumPerformerFromCueFileIfExists(audioFile))
        .orElse(UNKNOWN_ARTIST);
  }

  private String getAlbumName(AudioFile audioFile) {
    return Optional.ofNullable(AudioFileTagReader.readFromTag(audioFile, FieldKey.ALBUM))
        .filter(StringUtils::hasText)
        .or(() -> readAlbumTitleFromCueFileIfExists(audioFile))
        .orElse(UNKNOWN_ALBUM);
  }

  private String getAlbumReleaseDate(AudioFile audioFile) {
    var date = AudioFileTagReader.readFromTag(audioFile, FieldKey.ORIGINALRELEASEDATE);
    var yearTags = new LinkedList<>(List.of(FieldKey.YEAR, FieldKey.ALBUM_YEAR, FieldKey.ORIGINAL_YEAR));
    while (date == null && !yearTags.isEmpty()) {
      date = AudioFileTagReader.readFromTag(audioFile, yearTags.pop());
    }
    if (!StringUtils.hasText(date)) {
      date = readReleaseDateFromCueFileIfExists(audioFile).orElse(null);
    }
    return date;
  }

  private Optional<String> readAlbumPerformerFromCueFileIfExists(AudioFile audioFile) {
    var metadataFile = getCueFileIfExist(audioFile);
    return metadataFile.performer();
  }

  private Optional<String> readAlbumTitleFromCueFileIfExists(AudioFile audioFile) {
    var metadataFile = getCueFileIfExist(audioFile);
    return metadataFile.title();
  }

  private Optional<String> readReleaseDateFromCueFileIfExists(AudioFile audioFile) {
    var metadataFile = getCueFileIfExist(audioFile);
    return metadataFile.remDate();
  }

  private MetadataFile getCueFileIfExist(AudioFile audioFile) {
    var cueFileDir = audioFile.getFile().toPath().getParent();

    try (var files = Files.list(cueFileDir)) {

      var cueFiles = files
          .filter(path -> MetadataFileReader.Formats.CUE.getFormat().equals(FileUtils.getFileExtension(path)))
          .toList();

      if (cueFiles.isEmpty()) {
        return MetadataFile.emptyFile();
      }

      if (cueFiles.size() > 1) {
        // log matching files
        return MetadataFile.emptyFile();
      }

      return metadataFileReader.read(cueFiles.get(0).toString());

    } catch (IOException e) {
      return MetadataFile.emptyFile();
    }
  }
}