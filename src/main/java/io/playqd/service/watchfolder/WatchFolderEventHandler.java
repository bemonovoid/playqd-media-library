package io.playqd.service.watchfolder;

import io.playqd.model.event.AudioFileByteStreamRequestedEvent;
import io.playqd.model.event.WatchFolderModifiedEvent;
import io.playqd.persistence.AudioFileDao;
import io.playqd.service.watchfolder.WatchFolderScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
class WatchFolderEventHandler {

  private final AudioFileDao audioFileDao;
  private final WatchFolderScanner watchFolderScanner;

  WatchFolderEventHandler(AudioFileDao audioFileDao,
                          WatchFolderScanner watchFolderScanner) {
    this.audioFileDao = audioFileDao;
    this.watchFolderScanner = watchFolderScanner;
  }

  @EventListener(WatchFolderModifiedEvent.class)
  public void handleWatchFolderModifiedEvent(WatchFolderModifiedEvent event) {
    event.changedContentDirs().forEach(path -> watchFolderScanner.scan(event.watchFolder().id(), path));
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @EventListener(AudioFileByteStreamRequestedEvent.class)
  public void handleWatchedAudioFileStreamRequestedEvent(AudioFileByteStreamRequestedEvent event) {
    audioFileDao.updateAudioFileLastPlaybackDate(event.audioFileId());
  }
}