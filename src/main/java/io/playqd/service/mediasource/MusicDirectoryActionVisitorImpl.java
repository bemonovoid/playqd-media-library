package io.playqd.service.mediasource;

import io.playqd.commons.data.MusicDirectoryAction;
import io.playqd.commons.data.MusicDirectoryActionVisitor;
import io.playqd.commons.data.ScanAction;
import org.springframework.stereotype.Component;

@Component
class MusicDirectoryActionVisitorImpl implements MusicDirectoryActionVisitor {

  private final MusicDirectoryScanner musicDirectoryScanner;

  public MusicDirectoryActionVisitorImpl(MusicDirectoryScanner musicDirectoryScanner) {
    this.musicDirectoryScanner = musicDirectoryScanner;
  }

  @Override
  public void visit(MusicDirectoryAction action) {

  }

  @Override
  public void visit(ScanAction action) {
    visit((MusicDirectoryAction) action);
    musicDirectoryScanner.scan(action.getId());
  }
}
