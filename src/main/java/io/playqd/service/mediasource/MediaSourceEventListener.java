package io.playqd.service.mediasource;

import io.playqd.model.event.AudioFileByteStreamRequestedEvent;
import io.playqd.model.event.MusicDirectoryContentChangedEvent;
import io.playqd.persistence.AudioFileDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
class MediaSourceEventListener {

  private final AudioFileDao audioFileDao;
  private final MusicDirectoryScanner musicDirectoryScanner;

  MediaSourceEventListener(AudioFileDao audioFileDao,
                           MusicDirectoryScanner musicDirectoryScanner) {
    this.audioFileDao = audioFileDao;
    this.musicDirectoryScanner = musicDirectoryScanner;
  }

  @EventListener(MusicDirectoryContentChangedEvent.class)
  public void handleMediaSourceContentChangedEvent(MusicDirectoryContentChangedEvent event) {
    event.changedContentDirs().forEach(path -> musicDirectoryScanner.scan(event.musicDirectory().id(), path));
  }

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @EventListener(AudioFileByteStreamRequestedEvent.class)
  public void handleMediaSourceContentStreamRequested(AudioFileByteStreamRequestedEvent event) {
    audioFileDao.updateAudioFileLastPlaybackDate(event.audioFileId());
  }

}
