package io.playqd.service.watchfolder;

import io.playqd.commons.data.ItemType;
import io.playqd.commons.data.WatchFolderItem;
import io.playqd.commons.utils.FileUtils;
import io.playqd.persistence.WatchFolderDao;
import io.playqd.util.SupportedAudioFiles;
import io.playqd.util.SupportedImageFiles;
import io.playqd.util.UUIDV3Ids;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class WatchFolderBrowserImpl implements WatchFolderBrowser {

  private final Map<String, Path> paths = Collections.synchronizedMap(new HashMap<>());

  private final WatchFolderDao watchFolderDao;

  public WatchFolderBrowserImpl(WatchFolderDao watchFolderDao) {
    this.watchFolderDao = watchFolderDao;
  }

  @Override
  public Optional<WatchFolderItem> get(String itemId) {
    return Optional.ofNullable(paths.get(itemId)).map(this::toWatchFolderItem);
  }

  @Override
  public List<WatchFolderItem> browse() {
    return watchFolderDao.getAll().stream()
        .map(watchFolder -> toWatchFolderItem(watchFolder.path()))
        .peek(i -> paths.putIfAbsent(i.id(), i.path()))
        .toList();
  }

  @Override
  public List<WatchFolderItem> browse(String folderId, ItemType itemType) {
    return Optional.ofNullable(paths.get(folderId))
        .map(path -> browseInPath(path, itemType, Pageable.unpaged()).getContent())
        .orElseGet(Collections::emptyList);
  }

  @Override
  public Page<WatchFolderItem> browse(String folderId, Pageable pageable) {
    if (paths.containsKey(folderId)) {
      var path = paths.get(folderId);
      return browseInPath(path, null, pageable);
    } else {
      return Page.empty();
    }
  }

  private Page<WatchFolderItem> browseInPath(Path path, ItemType itemType, Pageable pageable) {
    Predicate<WatchFolderItem> filter = i -> {
      if (itemType != null) {
        return itemType == i.itemType();
      }
      return ItemType.otherFile != i.itemType();
    };
    try (var pathItems = Files.list(path)) {
      var items = pathItems
          .map(this::toWatchFolderItem)
          .filter(filter)
          .peek(i -> paths.putIfAbsent(i.id(), i.path()))
          .sorted(watchFolderItemComparator())
          .toList();
      if (pageable.isUnpaged()) {
        return new PageImpl<>(items, pageable, items.size());
      }
      var pageSize = pageable.getPageSize();
      if (pageSize > items.size()) {
        pageSize = items.size();
      }
      if (pageable.getPageNumber() == 0) {
        return new PageImpl<>(items.subList(0, pageSize), pageable, items.size());
      }
      var fromIdx = pageable.getPageSize() / pageable.getPageNumber();
      if (fromIdx > items.size()) {
        return Page.empty();
      }
      var toIdx = (pageable.getPageSize() + 1) * pageable.getPageNumber();
      if (toIdx > items.size()) {
        toIdx = items.size();
      }
      return new PageImpl<>(items.subList(fromIdx, toIdx), pageable, items.size());
    } catch (IOException e) {
      log.error("", e);
      return Page.empty(); //TODO throw
    }
  }

  private WatchFolderItem toWatchFolderItem(Path path) {
    var pathItemType = pathToItemType(path);
    var childItemsCounts = getChildItemsCount(path);
    return new WatchFolderItem(
        UUIDV3Ids.create(path.toString()),
        path.getFileName().toString(),
        path,
        ItemType.folder != pathItemType ? FileUtils.detectMimeType(path) : null,
        ItemType.folder != pathItemType ? FileUtils.getFileSize(path) : 0, // TODO calculate size for all
        pathItemType,
        childItemsCounts);
  }

  private static Comparator<WatchFolderItem> watchFolderItemComparator() {
    return (o1, o2) -> {
      var o1IsFolder = ItemType.folder == o1.itemType();
      var o2IsFolder = ItemType.folder == o2.itemType();
      if (o1IsFolder && o2IsFolder) {
        return o1.name().compareTo(o2.name());
      } else if (o1IsFolder) {
        return -1;
      } else if (o2IsFolder) {
        return 1;
      } else {
        return o1.name().compareTo(o2.name());
      }
    };
  }

  private static Map<ItemType, Long> getChildItemsCount(Path path) {
    if (Files.isRegularFile(path)) {
      return Collections.emptyMap();
    }
    try (var pathStream = Files.list(path)) {
      return pathStream
          .map(WatchFolderBrowserImpl::pathToItemType)
          .filter(itemType -> itemType != ItemType.otherFile)
          .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
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
      itemType = ItemType.otherFile;
    }
    return itemType;
  }
}
