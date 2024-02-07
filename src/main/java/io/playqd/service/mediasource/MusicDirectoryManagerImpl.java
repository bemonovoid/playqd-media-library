package io.playqd.service.mediasource;

import io.playqd.commons.data.DirectoryItem;
import io.playqd.commons.data.ItemType;
import io.playqd.commons.data.MusicDirectory;
import io.playqd.commons.data.MusicDirectoryContentInfo;
import io.playqd.exception.CounterException;
import io.playqd.persistence.MusicDirectoryDao;
import io.playqd.util.FileUtils;
import io.playqd.util.SupportedAudioFiles;
import io.playqd.util.SupportedImageFiles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class MusicDirectoryManagerImpl implements MusicDirectoryManager {

  private final MusicDirectoryDao musicDirectoryDao;

  public MusicDirectoryManagerImpl(MusicDirectoryDao musicDirectoryDao) {
    this.musicDirectoryDao = musicDirectoryDao;
  }

  @Override
  public MusicDirectory get(long sourceId) {
    return musicDirectoryDao.get(sourceId);
  }

  @Override
  public List<MusicDirectory> getAll() {
    return musicDirectoryDao.getAll();
  }

  @Override
  public MusicDirectory create(MusicDirectory musicDirectory) {
    return musicDirectoryDao.create(musicDirectory);
  }

  @Override
  public Page<DirectoryItem> tree(Pageable page) {
    var result = getAll().stream().map(this::toDirectoryItem).toList();
    return new PageImpl<>(result, page, result.size());
  }

  @Override
  public Page<DirectoryItem> tree(long id, String pathBase64Encoded, Pageable page) {
    var musicDirectory = get(id);
    var parentPath = musicDirectory.path();
    if (StringUtils.hasLength(pathBase64Encoded)) {
      var pathDecoded = new String(Base64.getDecoder().decode(pathBase64Encoded));
      parentPath = parentPath.resolve(pathDecoded);
    }
    try (var pathItems = Files.list(parentPath)) {
      var result = pathItems
          .map(p -> toDirectoryItem(musicDirectory, p))
          .toList();
      return new PageImpl<>(result, page, result.size());
    } catch (IOException e) {
      log.error("", e);
      return new PageImpl<>(Collections.emptyList(), page, 0);
    }
  }

  private DirectoryItem toDirectoryItem(MusicDirectory musicDirectory) {
    var childItemsCount = getChildItemsCount(musicDirectory.path());
    return new DirectoryItem(
        musicDirectory.id(),
        musicDirectory.name(),
        "music dir root",
        ItemType.folder,
        childItemsCount);
  }

  private DirectoryItem toDirectoryItem(MusicDirectory musicDirectory, Path path) {
    var childItemsCounts = getChildItemsCount(path);
    return new DirectoryItem(
        musicDirectory.id(),
        path.getFileName().toString(),
        musicDirectory.path().relativize(path).toString(),
        pathToItemType(path),
        childItemsCounts);
  }

  private static Map<ItemType, Long> getChildItemsCount(Path parentPath) {
    try (var pathStream = Files.list(parentPath)) {
      return pathStream.collect(Collectors.groupingBy(
          MusicDirectoryManagerImpl::pathToItemType,
          Collectors.counting()));
    } catch (IOException e) {
      log.error("", e);
      return Collections.emptyMap();
    }
  }

  private static ItemType pathToItemType(Path path) {
    var itemType = (ItemType) null;
    if (Files.isDirectory(path)) {
      itemType = ItemType.folder;
    } else if (SupportedAudioFiles.isSupportedAudioFile(path)) {
      itemType = ItemType.audioFile;
    } else if (SupportedImageFiles.isSupportedImageFile(path)) {
      itemType = ItemType.imageFile;
    } else {
      itemType = ItemType.file;
    }
    return itemType;
  }

  @Override
  public MusicDirectoryContentInfo info(long sourceId) {
    var mediaSource = get(sourceId);
    try (Stream<Path> filesStream = Files.walk(mediaSource.path())) {
      var extensionCounts = filesStream
          .filter(SupportedAudioFiles::isSupportedAudioFile)
          .collect(Collectors.groupingBy(FileUtils::getFileExtension, Collectors.counting()))
          .entrySet().stream()
          .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              Map.Entry::getValue,
              (e1, e2) -> e1,
              LinkedHashMap::new));
      var totalCount = extensionCounts.values().stream()
          .collect(Collectors.summarizingLong(value -> value))
          .getSum();
      return new MusicDirectoryContentInfo(mediaSource, totalCount, extensionCounts);
    } catch (IOException e) {
      throw new CounterException(e);
    }
  }

}
