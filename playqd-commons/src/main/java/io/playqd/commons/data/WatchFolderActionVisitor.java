package io.playqd.commons.data;

public interface WatchFolderActionVisitor {

  void visit(WatchFolderAction action);

  void visit(ScanAction action);
}
