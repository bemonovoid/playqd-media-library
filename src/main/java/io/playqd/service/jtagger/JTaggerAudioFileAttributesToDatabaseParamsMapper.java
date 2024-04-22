package io.playqd.service.jtagger;

import io.playqd.exception.AudioMetadataReadException;
import io.playqd.persistence.jpa.entity.AudioFileJpaEntity;
import io.playqd.service.MetadataFileReader;
import io.playqd.service.jtagger.JTaggerAudioFileUtils;
import io.playqd.service.metadata.CommonFileAttributesToSqlParamsMapper;
import io.playqd.service.metadata.MetadataFile;
import io.playqd.util.FileUtils;
import io.playqd.util.UUIDV3Ids;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.springframework.util.StringUtils;

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
   * @param audioFile to read audio metadata from
   * @return
   * @throws AudioMetadataReadException
   */
  public Map<String, Object> toSqlParams(AudioFile audioFile) {
    try {
      var file = audioFile.getFile();
      var fileName = FileUtils.getFileNameWithoutExtension(file.getName());

      var params = new LinkedHashMap<>(super.toSqlParams(audioFile));

      // Audio
      params.put(AudioFileJpaEntity.COL_FORMAT, audioFile.getAudioHeader().getFormat());
      params.put(AudioFileJpaEntity.COL_BIT_RATE, audioFile.getAudioHeader().getBitRate());
      params.put(AudioFileJpaEntity.COL_CHANNELS, audioFile.getAudioHeader().getChannels());
      params.put(AudioFileJpaEntity.COL_LOSSLESS, audioFile.getAudioHeader().isLossless());
      params.put(AudioFileJpaEntity.COL_SAMPLE_RATE, audioFile.getAudioHeader().getSampleRate());
      params.put(AudioFileJpaEntity.COL_ENCODING_TYPE, audioFile.getAudioHeader().getEncodingType());
      params.put(AudioFileJpaEntity.COL_BITS_PER_SAMPLE, audioFile.getAudioHeader().getBitsPerSample());

      // Artist
      var artistName = getArtistName(audioFile);
      var artistId = UUIDS.computeIfAbsent(artistName, UUIDV3Ids::create);

      params.put(AudioFileJpaEntity.COL_ARTIST_NAME, artistName);
      params.put(AudioFileJpaEntity.COL_ARTIST_ID, artistId);
      params.put(AudioFileJpaEntity.COL_ARTIST_COUNTRY,
          JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.COUNTRY));

      // Album
      var albumName = getAlbumName(audioFile);
      var albumId = UUIDS.computeIfAbsent(artistName + albumName, UUIDV3Ids::create);
      params.put(AudioFileJpaEntity.COL_ALBUM_NAME, albumName);
      params.put(AudioFileJpaEntity.COL_ALBUM_ID, albumId);
      params.put(AudioFileJpaEntity.COL_ALBUM_RELEASE_DATE, getAlbumReleaseDate(audioFile));

      // Track
      var trackName = JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.TITLE, () -> fileName);
      params.put(AudioFileJpaEntity.COL_TRACK_NAME, trackName);
      params.put(AudioFileJpaEntity.COL_TRACK_ID, UUIDV3Ids.create(file.getPath()));
      params.put(AudioFileJpaEntity.COL_TRACK_NUMBER,
          JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.TRACK));
      params.put(AudioFileJpaEntity.COL_TRACK_LENGTH, audioFile.getAudioHeader().getTrackLength());
      params.put(AudioFileJpaEntity.COL_PRECISE_TRACK_LENGTH,
          audioFile.getAudioHeader().getPreciseTrackLength());

      // Genre
      var genreName = JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.GENRE, () -> UNKNOWN_GENRE);
      var genreId = UUIDS.computeIfAbsent("genre:" + genreName, UUIDV3Ids::create);
      params.put(AudioFileJpaEntity.COL_GENRE, genreName);
      params.put(AudioFileJpaEntity.COL_GENRE_ID, genreId);

      // Artwork
      params.put(AudioFileJpaEntity.COL_ARTWORK_EMBEDDED, false);
      applyArtworkDetails(audioFile, artwork -> {
        if (artwork.getBinaryData() != null && artwork.getBinaryData().length > 0) {
          params.put(AudioFileJpaEntity.COL_ARTWORK_EMBEDDED, true);
        }
      });

      // Other
      params.put(AudioFileJpaEntity.COL_COMMENT,
          JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.COMMENT));
      params.put(AudioFileJpaEntity.COL_LYRICS,
          JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.LYRICS));
      params.put(AudioFileJpaEntity.COL_RATING,
          JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.RATING));

      // Musicbrainz
      params.put(AudioFileJpaEntity.COL_MB_ARTIST_ID,
          JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.MUSICBRAINZ_ARTISTID));
      params.put(AudioFileJpaEntity.COL_MB_TRACK_ID,
          JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.MUSICBRAINZ_TRACK_ID));
      params.put(AudioFileJpaEntity.COL_MB_RELEASE_TYPE,
          JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.MUSICBRAINZ_RELEASE_TYPE));
      params.put(AudioFileJpaEntity.COL_MB_RELEASE_GROUP_ID,
          JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID));

      // Defaults
      params.put(AudioFileJpaEntity.COL_FILE_PLAYBACK_COUNT, 0);

      return params;
    } catch (Exception e) {
      throw new AudioMetadataReadException(e);
    }
  }

  private String getArtistName(AudioFile audioFile) {
    return JTaggerAudioFileUtils.getArtistNameTagsOrdered().stream()
        .map(fieldKey -> JTaggerAudioFileUtils.readFromTag(audioFile, fieldKey))
        .filter(StringUtils::hasText)
        .findFirst()
        .or(() -> readAlbumPerformerFromCueFileIfExists(audioFile))
        .orElse(UNKNOWN_ARTIST);
  }

  private String getAlbumName(AudioFile audioFile) {
    return Optional.ofNullable(JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.ALBUM))
        .filter(StringUtils::hasText)
        .or(() -> readAlbumTitleFromCueFileIfExists(audioFile))
        .orElse(UNKNOWN_ALBUM);
  }

  private String getAlbumReleaseDate(AudioFile audioFile) {
    var date = JTaggerAudioFileUtils.readFromTag(audioFile, FieldKey.ORIGINALRELEASEDATE);
    var yearTags = new LinkedList<>(List.of(FieldKey.YEAR, FieldKey.ALBUM_YEAR, FieldKey.ORIGINAL_YEAR));
    while (date == null && !yearTags.isEmpty()) {
      date = JTaggerAudioFileUtils.readFromTag(audioFile, yearTags.pop());
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
    return metadataFileReader.findAndReadCueSheetFromDir(cueFileDir.toString());
  }
}