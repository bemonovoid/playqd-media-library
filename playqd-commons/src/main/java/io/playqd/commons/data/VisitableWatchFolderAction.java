package io.playqd.commons.data;

@FunctionalInterface
public interface VisitableWatchFolderAction {

  void accept(WatchFolderActionVisitor visitor);

}
