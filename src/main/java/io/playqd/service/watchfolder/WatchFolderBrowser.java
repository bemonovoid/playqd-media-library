package io.playqd.service.watchfolder;

import io.playqd.commons.data.ItemType;
import io.playqd.commons.data.WatchFolderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WatchFolderBrowser {

  Optional<WatchFolderItem> get(String itemId);

  List<WatchFolderItem> browse();

  List<WatchFolderItem> browse(String folderId, ItemType itemType);

  Page<WatchFolderItem> browse(String folderId, Pageable pageable);
}