package io.playqd.service.playlist;

import io.playqd.model.event.PlaylistCreatedEvent;
import io.playqd.model.event.PlaylistDeletedEvent;
import io.playqd.model.event.PlaylistModifiedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
class PlaylistEventHandler {

  @Async
  @EventListener(PlaylistCreatedEvent.class)
  public void handleEvent(PlaylistCreatedEvent event) {
    var playlistName = event.playlist().title();
  }

  @Async
  @EventListener(PlaylistModifiedEvent.class)
  public void handleEvent(PlaylistModifiedEvent event) {

  }

  @Async
  @EventListener(PlaylistDeletedEvent.class)
  public void handleEvent(PlaylistDeletedEvent event) {
    var playlistName = event.playlist().title();
  }
}
