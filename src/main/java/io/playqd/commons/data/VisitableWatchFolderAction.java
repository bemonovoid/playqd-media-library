package io.playqd.commons.data;

import io.playqd.commons.data.WatchFolderActionVisitor;

@FunctionalInterface
public interface VisitableWatchFolderAction {

  void accept(WatchFolderActionVisitor visitor);

}
