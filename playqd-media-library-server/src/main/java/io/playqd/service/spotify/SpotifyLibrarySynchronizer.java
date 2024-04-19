package io.playqd.service.spotify;

import com.google.gson.JsonParser;
import io.playqd.service.winamp.WinampLibrary;
import io.playqd.commons.data.Playlist;
import io.playqd.config.properties.PlayqdProperties;
import io.playqd.exception.PlayqdException;
import io.playqd.model.event.WinampLibraryCreated;
import io.playqd.model.event.WinampLibraryModified;
import io.playqd.model.event.PlaylistCreatedEvent;
import io.playqd.model.event.PlaylistDeletedEvent;
import io.playqd.model.event.PlaylistModifiedEvent;
import io.playqd.persistence.AudioFileDao;
import io.playqd.service.spotify.client.SpotifyApiContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SpotifyLibrarySynchronizer {

  private final String spotifyUserId;
  private final AudioFileDao audioFileDao;
  private final SpotifyApiContext spotifyApiContext;
  private final SpotifyTracksSearch spotifyTracksSearch;

  public SpotifyLibrarySynchronizer(AudioFileDao audioFileDao,
                                    PlayqdProperties playqdProperties,
                                    SpotifyApiContext spotifyApiContext,
                                    SpotifyTracksSearch spotifyTracksSearch) {
    this.audioFileDao = audioFileDao;
    this.spotifyApiContext = spotifyApiContext;
    this.spotifyTracksSearch = spotifyTracksSearch;
    this.spotifyUserId = playqdProperties.getSpotify().getUserId();
  }

  @Async
  @EventListener(WinampLibraryCreated.class)
  public void handleEvent(WinampLibraryCreated event) {
    syncRatedTracks(event.winampLibrary());
  }

  @Async
  @EventListener(WinampLibraryModified.class)
  public void handleEvent(WinampLibraryModified event) {
    syncRatedTracks(event.winampLibrary());
  }

  private void sync(PlaylistModifiedEvent event) {
    var newName = event.newPlaylist().title();
    var oldName = event.oldPlaylist().title();

    findPlaylistByName(oldName).ifPresentOrElse(spotifyPlaylist -> {
      if (!spotifyPlaylist.getName().equals(newName)) {
        renameSpotifyPlaylist(spotifyPlaylist.getId(), newName);
        log.info("Spotify playlist with id: '{}' was renamed from '{}' to '{}'",
            spotifyPlaylist.getId(), oldName, newName);
      }
      syncPlaylistItems(event.newPlaylist(), spotifyPlaylist);
    }, () -> log.info("Spotify playlist with name: '{}' was not found.", event.oldPlaylist().title()));
  }

  private void syncRatedTracks(WinampLibrary winampLibrary) {
    var rated = new LinkedList<String>();
    var recentlyPlayed = new LinkedList<String>();

    for (var r : winampLibrary.data().getRecords()) {
      if (r.rating() != null && r.rating() == 5) {
        rated.add(r.fileName());
      }
      if (r.wasPlayed() && winampLibrary.fileLastModifiedDate().isBefore((r.lastPlay()))) {
        recentlyPlayed.add(r.fileName());
      }
    }

    var ratedAudioFiles = audioFileDao.getAudioFilesByLocationIn(rated);

    log.info("Found {} new rated tracks.", rated.size());

    var savedTracksBeforeUpdate =
        spotifyApiContext.executeOrThrow(spotifyApi -> spotifyApi.getUsersSavedTracks().build());

    ratedAudioFiles.getContent().stream()
        .map(spotifyTracksSearch::search)
        .filter(Objects::nonNull)
        .forEach(t -> spotifyApiContext.executeOrThrow(spotifyApi -> spotifyApi.saveTracksForUser(t.id()).build()));

    var savedTracksAfterUpdate =
        spotifyApiContext.executeOrThrow(spotifyApi -> spotifyApi.getUsersSavedTracks().build());

    log.info("Saved {} user's track(s).",
        savedTracksAfterUpdate.getData().getTotal() - savedTracksBeforeUpdate.getData().getTotal());
  }

  private void syncPlaylistItems(Playlist playlist, se.michaelthelin.spotify.model_objects.specification.Playlist spotifyPlaylist) {
    var audioFiles =
        audioFileDao.getAudioFilesFromPlaylist(playlist, Pageable.unpaged()).getContent();

    log.info("Syncing {} playlist item(s) from '{}-{}'.",
        audioFiles.size(), playlist.fileName(), playlist.title());

    log.info("Matched {} Winamp playlist tracks(s) with {} library tracks(s).", audioFiles.size(), audioFiles.size());

    var matchedTrackIds = audioFiles.stream()
        // Force db look-up again because some spotify data could have been updated.
        .map(audioFile -> audioFileDao.getAudioFileByTrackId(audioFile.trackId()))
        .map(spotifyTracksSearch::search)
        .filter(Objects::nonNull)
        .toList();

    log.info("Matched {} library track(s) with {} Spotify track(s)", audioFiles.size(), matchedTrackIds.size());

    var spotifyPlaylistTracks = spotifyPlaylist.getTracks();

    var spotifyPlaylistTrackUris = Arrays.stream(spotifyPlaylistTracks.getItems())
        .collect(Collectors.toMap(k -> k.getTrack().getUri(), v -> v));

    var newTrackUris = new ArrayList<String>();

    for (var itUri : matchedTrackIds) {
      if (spotifyPlaylistTrackUris.remove(itUri.uri()) == null) {
        newTrackUris.add(itUri.uri());
      }
    }

    if (!newTrackUris.isEmpty()) {
      addItemsToPlaylist(spotifyPlaylist, newTrackUris.toArray(String[]::new));
    }
    if (!spotifyPlaylistTrackUris.isEmpty()) {
      deleteItemsFromPlaylist(spotifyPlaylist, spotifyPlaylistTrackUris.values());
    }
  }

  private se.michaelthelin.spotify.model_objects.specification.Playlist createSpotifyPlaylist(String name) {

    log.info("Spotify user's playlist with name '{}' wasn't found. Creating new playlist ...", name);

    var spotifyResponse =
        spotifyApiContext.execute(spotifyApi -> spotifyApi.createPlaylist(spotifyUserId, name).public_(false).build());

    if (spotifyResponse.isSuccess()) {
      log.info("New playlist with name '{}' was successfully created", spotifyResponse.getData().getName());
      return spotifyResponse.getData();
    } else {
      throw new PlayqdException(spotifyResponse.getError());
    }
  }

  private void addItemsToPlaylist(se.michaelthelin.spotify.model_objects.specification.Playlist playlist, String[] uris) {
    var spotifyResponse =
        spotifyApiContext.execute(spotifyApi -> spotifyApi.addItemsToPlaylist(playlist.getId(), uris).build());

    if (spotifyResponse.isSuccess()) {
      log.info("Successfully added {} tracks to Spotify playlist: {}", uris.length, playlist.getName());
    } else {
      log.error("Adding track items to playlist failed.");
    }
  }

  private Map<String, PlaylistSimplified> retrieveSpotifyPlaylists() {
    var spotifyResponse =
        spotifyApiContext.execute(spotifyApi -> spotifyApi.getListOfUsersPlaylists(spotifyUserId).build());
    if (spotifyResponse.hasError()) {
      throw new PlayqdException(spotifyResponse.getError());
    }
    return Stream.of(spotifyResponse.getData().getItems())
        .filter(p -> p.getName().startsWith("(x_"))
        .collect(Collectors.toMap(PlaylistSimplified::getId, v -> v));
  }

  private se.michaelthelin.spotify.model_objects.specification.Playlist retrieveSpotifyPlaylist(String id) {
    var spotifyResponse = spotifyApiContext.execute(spotifyApi -> spotifyApi.getPlaylist(id).build());
    if (spotifyResponse.hasError()) {
      throw new PlayqdException(spotifyResponse.getError());
    }
    return spotifyResponse.getData();
  }

  private void renameSpotifyPlaylist(String id, String newName) {
    var spotifyResponse =
        spotifyApiContext.execute(spotifyApi -> spotifyApi.changePlaylistsDetails(id).name(newName).build());
    if (spotifyResponse.hasError()) {
      throw new PlayqdException(spotifyResponse.getError());
    }
  }

  private void deleteSpotifyPlaylist(se.michaelthelin.spotify.model_objects.specification.Playlist playlist) {
    deleteItemsFromPlaylist(playlist);
  }

  private void deleteItemsFromPlaylist(se.michaelthelin.spotify.model_objects.specification.Playlist playlist) {
    deleteItemsFromPlaylist(playlist, Arrays.asList(playlist.getTracks().getItems()));
  }

  private void deleteItemsFromPlaylist(se.michaelthelin.spotify.model_objects.specification.Playlist playlist, Collection<PlaylistTrack> tracks) {
    var itemObj = "{\"uri\":\"spotify:track:%s\"}";

    var jsonObjects = tracks.stream()
        .map(t -> t.getTrack().getId())
        .map(u -> String.format(itemObj, u))
        .collect(Collectors.joining(","));

    var jsonArray = JsonParser.parseString("["  + jsonObjects + "]").getAsJsonArray();

    var spotifyUpdatePlaylistResponse = spotifyApiContext.execute(spotifyApi -> spotifyApi.removeItemsFromPlaylist(
        playlist.getId(), jsonArray).build());

    if (spotifyUpdatePlaylistResponse.isSuccess()) {
      log.info("Successfully deleted {} track items(s) from Spotify playlist '{}'", tracks.size(), playlist.getName());
    } else {
      log.warn("Deleting track item(s) form playlist failed.");
    }
  }

  private Optional<se.michaelthelin.spotify.model_objects.specification.Playlist> findPlaylistByName(String name) {
    return retrieveSpotifyPlaylists().entrySet().stream()
        .filter(entry -> entry.getValue().getName().equals(name))
        .findFirst()
        .map(p -> retrieveSpotifyPlaylist(p.getValue().getId()));
  }
}