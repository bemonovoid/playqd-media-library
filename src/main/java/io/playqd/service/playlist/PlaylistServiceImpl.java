package io.playqd.service.playlist;

import io.playqd.commons.data.Playlist;
import io.playqd.commons.data.Playlists;
import io.playqd.commons.utils.Tuple;
import io.playqd.model.event.PlaylistCreatedEvent;
import io.playqd.model.event.PlaylistDeletedEvent;
import io.playqd.model.event.PlaylistModifiedEvent;
import io.playqd.persistence.PlaylistDao;
import io.playqd.service.playlist.PlaylistFetcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
class PlaylistServiceImpl implements PlaylistService {

  private final PlaylistDao playlistDao;
  private final List<PlaylistFetcher> playlistFetchers;
  private final ApplicationEventPublisher eventPublisher;

  PlaylistServiceImpl(PlaylistDao playlistDao,
                      List<PlaylistFetcher> playlistFetchers,
                      ApplicationEventPublisher eventPublisher) {
    this.playlistDao = playlistDao;
    this.eventPublisher = eventPublisher;
    this.playlistFetchers = playlistFetchers;
  }

  @Override
  public Playlists getPlaylists() {
    return new Playlists(playlistDao.getPlaylists());
  }

  @Override
  public Playlists refreshPlaylists() {
    refreshPlaylistsPeriodically();
    return new Playlists(playlistDao.getPlaylists());
  }

  @Override
  public Optional<Playlist> getById(long id) {
    return playlistDao.getById(id);
  }

  @Override
  public Optional<Playlist> getByUUID(String uuid) {
    return playlistDao.getByUUID(uuid);
  }

  @Scheduled(cron = "0 0 * * * *")
  void refreshPlaylistsPeriodically() {
    var fetchedPlaylists = playlistFetchers.stream().flatMap(fetcher -> fetcher.fetch().stream()).toList();
    fetchedPlaylists.stream()
        .map(fp -> Tuple.from(fp, playlistDao.getByUUID(fp.uuid()).orElse(fp)))
        .forEach(t -> {
          if (t.right().id() < 0) {
            eventPublisher.publishEvent(new PlaylistCreatedEvent(playlistDao.create(t.right())));
          } else if (t.left().fileLastModifiedDate().isAfter(t.right().fileLastModifiedDate())) {
            eventPublisher.publishEvent(new PlaylistModifiedEvent(t.right(), playlistDao.update(t.left())));
          } else if (!Files.exists(t.right().location())) {
            playlistDao.delete(t.right());
            eventPublisher.publishEvent(new PlaylistDeletedEvent(t.right()));
          }
        });
  }
}