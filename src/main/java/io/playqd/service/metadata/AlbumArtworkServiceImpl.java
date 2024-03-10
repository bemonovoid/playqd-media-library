package io.playqd.service.metadata;

import io.playqd.commons.data.ArtworkSize;
import io.playqd.config.cache.CacheNames;
import io.playqd.model.AudioFile;
import io.playqd.persistence.AudioFileDao;
import io.playqd.util.FileUtils;
import io.playqd.util.ImageUtils;
import io.playqd.util.SupportedImageFiles;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
class AlbumArtworkServiceImpl implements AlbumArtworkService {

  private final AudioFileDao audioFileDao;

  public AlbumArtworkServiceImpl(AudioFileDao audioFileDao) {
    this.audioFileDao = audioFileDao;
  }

  @Override
  @Cacheable(cacheNames = CacheNames.ALBUM_ART_BY_ARTOWRK_KEY, unless = "#result == null")
  public Optional<Artwork> getArtwork(ArtworkKey artworkKey) {
    var audioFile = audioFileDao.getFirstAudioFileByAlbumId(artworkKey.getAlbumId());
    return getEmbedded(audioFile, artworkKey.getArtworkSize())
        .or(() -> getFromAlbumFolder(audioFile, artworkKey.getArtworkSize()));
  }

  private Optional<Artwork> getEmbedded(AudioFile audioFile, ArtworkSize artworkSize) {
    try {

      log.info("Getting album art from audio file metadata for '{} - {}'",
          audioFile.artistName(), audioFile.albumName());

      var jTaggerAudioFile = AudioFileIO.read(audioFile.path().toFile());

      var artwork = jTaggerAudioFile.getTag().getFirstArtwork();

      if (artwork == null) {
        return Optional.empty();
      }

      if (artwork.getBinaryData() == null || artwork.getBinaryData().length == 0) {
        log.warn("Album art in audio file metadata wasn't found.");
        return Optional.empty();
      }

      var binaryData = ImageUtils.resize(artwork.getBinaryData(), artworkSize);
      var mimeType = FileUtils.detectMimeType(binaryData);

      log.info("Album art was found in audio file metadata.");

      return Optional.of(new Artwork(binaryData.length, mimeType, binaryData));
    } catch (Exception e) {
      log.error("Failed to read audio file metadata at: {}", audioFile.path(), e);
      return Optional.empty();
    }
  }

  private Optional<Artwork> getFromAlbumFolder(AudioFile audioFile, ArtworkSize artworkSize) {
    var albumFolder = audioFile.path().getParent();

    log.info("Getting album art from album folder for '{} - {}' in {}",
        audioFile.artistName(), audioFile.albumName(), albumFolder);

    try (Stream<Path> albumFolderFilesStream = Files.list(albumFolder)) {
      var mayBeAlbumArt = albumFolderFilesStream
          .filter(Files::isRegularFile)
          .filter(path -> SupportedImageFiles.isAlbumArtFile(path, audioFile))
          .findFirst()
          .map(path -> createAlbumArtFromAlbumFolderPath(path, artworkSize));
      mayBeAlbumArt.ifPresentOrElse(
          artwork -> log.info("Found album art image in album folder"),
          () -> log.warn("Album art wasn't found"));
      return mayBeAlbumArt;
    } catch (Exception e) {
      log.error("Album artwork search failed at externalUrl: {}. {}", audioFile.path(), e.getMessage());
      return Optional.empty();
    }
  }

  private Artwork createAlbumArtFromAlbumFolderPath(Path path, ArtworkSize artworkSize) {
    var mimeType = FileUtils.detectMimeType(path.toString());
    try {
      var binaryData = ImageUtils.resize(Files.readAllBytes(path), artworkSize);
      return new Artwork(binaryData.length, mimeType, binaryData);
    } catch (IOException e) {
      log.error("Failed to read album folder image file content.", e);
      return new Artwork(0, null, new byte[]{});
    }
  }
}