package io.playqd.commons.data;

import io.playqd.commons.data.WatchFolderAction;

public interface WatchFolderActionVisitor {

  void visit(WatchFolderAction action);

  void visit(ScanAction action);
}
