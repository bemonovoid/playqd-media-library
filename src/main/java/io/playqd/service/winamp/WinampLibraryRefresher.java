package io.playqd.service.winamp;

import io.playqd.commons.data.Playlist;
import io.playqd.commons.utils.Tuple;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.model.event.WinampLibraryCreated;
import io.playqd.model.event.WinampLibraryModified;
import io.playqd.model.event.PlaylistCreatedEvent;
import io.playqd.model.event.PlaylistModifiedEvent;
import io.playqd.persistence.AudioFileDao;
import io.playqd.persistence.PlaylistDao;
import io.playqd.persistence.WinampLibraryDao;
import io.playqd.service.playlist.PlaylistFetcher;
import io.playqd.service.winamp.WinampLibrary;
import io.playqd.service.winamp.nde.NdeData;
import io.playqd.service.winamp.nde.NdeTrackRecord;
import io.playqd.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
//@Component
public class WinampLibraryRefresher {

  private final Path indexFilePath;
  private final Path dataFilePath;
  private final AudioFileDao audioFileDao;
  private final WinampLibraryDao winampDataDao;
  private final PlaylistDao playlistDao;
  private final ApplicationEventPublisher eventPublisher;
  private final PlaylistFetcher playlistFetcher;

  public WinampLibraryRefresher(AudioFileDao audioFileDao,
                                WinampLibraryDao winampDataDao,
                                PlayqdProperties playqdProperties,
                                PlaylistDao playlistDao,
                                ApplicationEventPublisher eventPublisher,
                                PlaylistFetcher playlistFetcher) {
    this.audioFileDao = audioFileDao;
    this.winampDataDao = winampDataDao;
    this.eventPublisher = eventPublisher;
    this.playlistFetcher = playlistFetcher;
    this.playlistDao = playlistDao;
    this.indexFilePath = Path.of(playqdProperties.getWinamp().getDir(), playqdProperties.getWinamp().getIndexFile());
    this.dataFilePath = Path.of(playqdProperties.getWinamp().getDir(), playqdProperties.getWinamp().getDataFile());
  }

//  @Scheduled(cron = "0 0 * * * *")
  public void refresh() {
//    refreshData();
//    refreshPlaylists();
  }

  private void refreshData() {
    try {
      var ndeData = NdeData.read(dataFilePath.toString(), indexFilePath.toString());

      var winampData = winampDataDao.get().orElseGet(() -> createNewWinampData(dataFilePath, ndeData));

      if (winampData.isNew()) {
        winampDataDao.create(winampData);
        eventPublisher.publishEvent(new WinampLibraryCreated(winampData));
        return;
      }
      var dataFileLastModifiedDate = getFileLastModifiedDate(dataFilePath);

      if (dataFileLastModifiedDate.isAfter(winampData.fileLastModifiedDate())) {
        winampDataDao.update(createNewWinampData(dataFilePath, ndeData));
        eventPublisher.publishEvent(new WinampLibraryModified(winampData));
      }
    } catch (IOException e) {
      log.error("Failed reading nde data.", e);
    }
  }

  private void refreshPlaylists() {
    log.info("Refreshing playlists ...");

    var actualPlaylists = playlistFetcher.fetch();

    var winampPlaylistVersions = playlistDao.getPlaylists().stream()
        .collect(Collectors.toMap(Playlist::uuid, v -> v));

    log.info("Fetched {} playlist(s) from Winamp and {} playlist(s) from last refresh.",
        actualPlaylists.size(), winampPlaylistVersions.size());

    for (var playlist : actualPlaylists) {
      var oldPlaylist = winampPlaylistVersions.get(playlist.uuid());
      if (oldPlaylist == null) {
        log.info("Found new playlist: {}", playlist.location());
        eventPublisher.publishEvent(new PlaylistCreatedEvent(playlistDao.create(playlist)));
      }
//      else if (!playlist.exists()) {
//        log.warn("Found {} obsolete playlist.", playlist);
//        playlistDao.delete(playlist);
//        eventPublisher.publishEvent(new WinampPlaylistDeletedEvent(playlist));
//      }
      else if (playlist.fileLastModifiedDate().isAfter(oldPlaylist.fileLastModifiedDate())) {
        log.info("Found modified playlists: {}", playlist.location());
        playlistDao.update(playlist);
        eventPublisher.publishEvent(new PlaylistModifiedEvent(oldPlaylist, playlist));
      }
    }
    log.info("Playlists refresh completed.");
  }

  record Views(HashMap<Path, NdeTrackRecord> rated, HashMap<Path, NdeTrackRecord> recentlyPlayed) { }

  private Views getUpdatedViews(WinampLibrary oldWinampLibrary, WinampLibrary newWinampLibrary) {
    var rated = new HashMap<Path, NdeTrackRecord>();
    var recentlyPlayed = new HashMap<Path, NdeTrackRecord>();

    Consumer<WinampLibrary> populate = winampLibrary -> {
      for (var r : winampLibrary.data().getRecords()) {
        var key = Paths.get(r.fileName());
        if ((r.rating() != null && r.rating() == 5 && rated.remove(key) == null)) {
          rated.put(key, r);
        }
        if (r.wasPlayed()) {
          var oldCount = recentlyPlayed.remove(key);
          if (oldCount != null) {

          }
          recentlyPlayed.put(key, r);
        }
      }
    };

    if (oldWinampLibrary != null) {
      populate.accept(oldWinampLibrary);
    }
    populate.accept(newWinampLibrary);

    return new Views(rated, recentlyPlayed);
  }

  private void updateAudioFileLibrary(WinampLibrary oldWinampLibrary, WinampLibrary newWinampLibrary) {
    var views = getUpdatedViews(oldWinampLibrary, newWinampLibrary);

    var locations = views.rated.keySet().stream().map(Object::toString).toList();

    var ratedAudioFiles = audioFileDao.getAudioFilesByLocationIn(locations).getContent().stream()
//        .filter(audioFile -> !audioFile.rated())
//        .peek(audioFile -> audioFileDao.updateRating(audioFile.id(), oldWinampLibrary.fileLastModifiedDate()))
        .toList();

    locations = views.recentlyPlayed.keySet().stream().map(Object::toString).toList();

    var recentlyPlayedAudioFiles = audioFileDao.getAudioFilesByLocationIn(locations).getContent().stream()
        .filter(audioFile -> views.recentlyPlayed.containsKey(audioFile.path()))
        .map(audioFile -> Tuple.from(audioFile, views.recentlyPlayed.get(audioFile.path())))
        .filter(audioFileNdeTrack -> {
          if (audioFileNdeTrack.left().fileLastPlaybackDate() == null) {
            return true;
          }
          var oldLastPlayedDate =
              LocalDateTime.ofInstant(audioFileNdeTrack.left().fileLastPlaybackDate(), ZoneId.systemDefault());
          return oldLastPlayedDate.isBefore(audioFileNdeTrack.right().lastPlay());
        })
        .peek(audioFileNdeTrack -> audioFileDao.updateAudioFileLastPlaybackDate(audioFileNdeTrack.left().id()))
        .toList();
  }

  private static WinampLibrary createNewWinampData(Path dataFilePath, NdeData ndeData) {
    return new WinampLibrary(
        -1,
        dataFilePath.getFileName().toString(),
        dataFilePath,
        ndeData,
        getFileLastModifiedDate(dataFilePath)
    );
  }

  private static LocalDateTime getFileLastModifiedDate(Path path) {
    try {
      var fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
      return FileUtils.getLastModifiedDate(fileAttributes.lastModifiedTime());
    } catch (IOException e) {
      throw new RuntimeException("Failed to read file's last modified date.", e);
    }
  }
}
