package io.playqd.service.watchfolder;

import io.playqd.commons.data.ScanAction;
import io.playqd.commons.data.WatchFolderAction;
import io.playqd.commons.data.WatchFolderActionVisitor;
import org.springframework.stereotype.Component;

@Component
class WatchFolderActionVisitorImpl implements WatchFolderActionVisitor {

  private final WatchFolderScanner watchFolderScanner;

  public WatchFolderActionVisitorImpl(WatchFolderScanner watchFolderScanner) {
    this.watchFolderScanner = watchFolderScanner;
  }

  @Override
  public void visit(WatchFolderAction action) {

  }

  @Override
  public void visit(ScanAction action) {
    visit((WatchFolderAction) action);
    watchFolderScanner.scan(action.getId());
  }
}
