package io.playqd.service.watchfolder;

import io.playqd.commons.data.WatchFolderContentInfo;

public interface WatchFolderService {

  WatchFolderContentInfo info(long sourceId);
}