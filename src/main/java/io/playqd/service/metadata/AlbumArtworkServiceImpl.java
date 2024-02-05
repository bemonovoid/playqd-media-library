package io.playqd.service.metadata;

import io.playqd.config.cache.CacheNames;
import io.playqd.model.AudioFile;
import io.playqd.persistence.AudioFileDao;
import io.playqd.service.AudioFilePathResolver;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Service
class AlbumArtworkServiceImpl implements AlbumArtworkService {

  private static final int IMAGE_WIDTH_SMALL = 250;
  private static final int IMAGE_HEIGHT_SMALL = 250;

  private final AudioFileDao audioFileDao;
  private final AudioFilePathResolver audioFilePathResolver;

  public AlbumArtworkServiceImpl(AudioFileDao audioFileDao, AudioFilePathResolver audioFilePathResolver) {
    this.audioFileDao = audioFileDao;
    this.audioFilePathResolver = audioFilePathResolver;
  }

  @Override
  @Cacheable(cacheNames = CacheNames.ALBUM_ART_BY_ALBUM_ID, unless = "#result == null")
  public Optional<Artwork> get(String albumId) {
    return get(audioFileDao.getFirstAudioFileByAlbumId(albumId));
  }

  @Override
  @Cacheable(cacheNames = CacheNames.ALBUM_ART_BY_ALBUM_ID, key = "#albumId", unless = "#result == null")
  public Optional<Artwork> get(String albumId, String albumFolderImageFileName) {
    var audioFile = audioFileDao.getFirstAudioFileByAlbumId(albumId);
    var albumFolder = audioFile.path().getParent();
    try (Stream<Path> albumFolderFilesStream = Files.list(albumFolder)) {
      return albumFolderFilesStream
          .filter(path -> path.endsWith(albumFolderImageFileName))
          .findFirst()
          .map(path -> createAlbumArtFromAlbumFolderPath(path, audioFile));
    } catch (IOException e) {
      log.error("Failed to read album folder image file: {}", albumFolderImageFileName, e);
      return Optional.empty();
    }
  }

  @Override
  @Cacheable(cacheNames = CacheNames.ALBUM_ART_BY_ALBUM_ID, key = "#audioFile.albumId", unless = "#result == null")
  public Optional<Artwork> get(AudioFile audioFile) {
    return getEmbedded(audioFile).or(() -> getFromAlbumFolder(audioFile));
  }

  private Optional<Artwork> getEmbedded(AudioFile audioFile) {
    try {

      log.info("Getting album art from audio file metadata for '{} - {}'",
          audioFile.artistName(), audioFile.albumName());

      var audioFilePath = audioFilePathResolver.unRelativize(audioFile);

      var jTaggerAudioFile = AudioFileIO.read(audioFilePath.toFile());

      var artwork = jTaggerAudioFile.getTag().getFirstArtwork();

      if (artwork == null) {
        return Optional.empty();
      }

      if (artwork.getBinaryData() == null || artwork.getBinaryData().length == 0) {
        log.warn("Album art in audio file metadata wasn't found.");
        return Optional.empty();
      }

      var imageByteArray = artwork.getBinaryData();
      var mimeType = FileUtils.detectMimeType(imageByteArray);
      var metadata = new ImageMetadata(
          imageByteArray.length, mimeType, new Dimensions(artwork.getWidth(), artwork.getHeight()));

      log.info("Album art was found in audio file metadata.");

      return Optional.of(new Artwork(audioFile.albumId(),
          createAlbumArtImageResources(audioFile.albumId(), null, imageByteArray), metadata));
    } catch (Exception e) {
      log.error("Failed to read audio file metadata at: {}", audioFile.path(), e);
      return Optional.empty();
    }
  }

  private Optional<Artwork> getFromAlbumFolder(AudioFile audioFile) {
    var location = audioFilePathResolver.unRelativize(audioFile);
    var albumFolder = location.getParent();

    log.info("Getting album art from album folder for '{} - {}' in {}",
        audioFile.artistName(), audioFile.albumName(), albumFolder);

    try (Stream<Path> albumFolderFilesStream = Files.list(albumFolder)) {
      var mayBeAlbumArt = albumFolderFilesStream
          .filter(Files::isRegularFile)
          .filter(path -> SupportedImageFiles.isAlbumArtFile(path, audioFile))
          .findFirst()
          .map(path -> createAlbumArtFromAlbumFolderPath(path, audioFile));
      mayBeAlbumArt.ifPresentOrElse(
          artwork -> log.info("Found album art image in album folder"),
          () -> log.warn("Album art wasn't found"));
      return mayBeAlbumArt;
    } catch (Exception e) {
      log.error("Album artwork search failed at externalUrl: {}. {}", location, e.getMessage());
      return Optional.empty();
    }
  }

  private Artwork createAlbumArtFromAlbumFolderPath(Path path, AudioFile audioFile) {
    var mimeType = FileUtils.detectMimeType(path.toString());
    var metadata = new ImageMetadata(FileUtils.getFileSize(path), mimeType, Dimensions.unknown());
    var imageByteArray = new byte[0];
    try {
      imageByteArray = Files.readAllBytes(path);
    } catch (IOException e) {
      log.error("Failed to read album folder image file content.", e);
    }
    return new Artwork(audioFile.albumId(), createAlbumArtImageResources(
        audioFile.albumId(), path.getFileName().toString(), imageByteArray), metadata);
  }

  private ImageResources createAlbumArtImageResources(String albumId,
                                                      String albumFolderImageFilename,
                                                      byte[] originalData) {
    return new ImageResources(
        new ImageResource(ImageUtils.createAlbumArtResourceUri(
            "localhost", albumId, albumFolderImageFilename), originalData),
        Map.of(ImageSizeRequestParam.sm,
            new ImageResource(ImageUtils.createAlbumArtResourceUri(
                "localhost", albumId, albumFolderImageFilename, ImageSizeRequestParam.sm),
                ImageUtils.resize(originalData, IMAGE_WIDTH_SMALL, IMAGE_HEIGHT_SMALL))));
  }

}